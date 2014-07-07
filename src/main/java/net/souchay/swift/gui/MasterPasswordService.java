package net.souchay.swift.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import net.souchay.utilities.Base64Codec;

public class MasterPasswordService {

    /**
     * Exception
     * 
     * @copyright Pierre Souchay - 2013,2014
     * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
     * @version $Revision: 3830 $
     * 
     */
    public static class MasterPasswordServiceNotAvailableException extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = -3770033383403394176L;

        /**
         * Constructor
         * 
         * @param reason
         */
        public MasterPasswordServiceNotAvailableException(String reason) {
            super(reason);
        }
    }

    private final static Charset CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$

    private static MasterPasswordService instance;

    public char[] checkMasterPassword(Component component) throws MasterPasswordServiceNotAvailableException {
        File fx = new File(ConfigurationPersistance.getInstance().getConfigurationDirectory(), "masterPassword.dat"); //$NON-NLS-1$
        char password[] = null;
        if (!fx.exists() || !fx.isFile() || !fx.canRead() || fx.length() < 10) {
            JPasswordField p1 = new JPasswordField();
            JPasswordField p2 = new JPasswordField();
            JPanel px = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = 2;
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.ipady = 5;
            px.add(new JLabel(Messages.getString("masterPasswordInfo")), c); //$NON-NLS-1$
            c.gridwidth = 1;
            c.weightx = 0;
            c.weighty = 0;
            c.gridy = 1;
            c.anchor = GridBagConstraints.BASELINE_TRAILING;
            c.ipadx = 5;
            c.gridx = 0;
            c.fill = GridBagConstraints.NONE;
            {
                JLabel lbl = new JLabel(Messages.getString("password")); //$NON-NLS-1$
                lbl.setLabelFor(p1);
                px.add(lbl, c);
                c.gridy++;
            }
            {
                JLabel lbl = new JLabel(Messages.getString("confirmPassword")); //$NON-NLS-1$
                lbl.setLabelFor(p1);
                px.add(lbl, c);
                c.gridy++;
            }
            c.gridy = 1;
            c.gridx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.BASELINE_LEADING;
            c.weightx = 1;
            px.add(p1, c);
            c.gridy++;
            px.add(p2, c);
            c.gridy++;
            int ret = JOptionPane.NO_OPTION;
            do {
                ret = JOptionPane.showConfirmDialog(component, px, Messages.getString("pleaseEnterMasterPassword"), //$NON-NLS-1$
                                                    JOptionPane.OK_CANCEL_OPTION);
                if (ret == JOptionPane.CANCEL_OPTION) {
                    throw new MasterPasswordServiceNotAvailableException(Messages.getString("cancelledByUser")); //$NON-NLS-1$
                }
                if (!Arrays.equals(p1.getPassword(), p2.getPassword())) {
                    JOptionPane.showMessageDialog(component, Messages.getString("passwordsDoNotMatch")); //$NON-NLS-1$
                }
            } while (!Arrays.equals(p1.getPassword(), p2.getPassword()) && p1.getPassword().length > 0);
            password = p1.getPassword();
            try {
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fx), CHARSET));
                try {
                    w.append(encode(password, password));
                } catch (Exception err) {
                    err.printStackTrace();
                } finally {
                    try {
                        w.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException err) {
                err.printStackTrace();
            }
        }

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fx), CHARSET));
            String toDecode;
            try {
                toDecode = in.readLine();
            } catch (IOException err) {
                fx.delete();
                throw new MasterPasswordServiceNotAvailableException("cannot read " + fx.getAbsolutePath()); //$NON-NLS-1$
            } finally {
                try {
                    in.close();
                } catch (IOException ignored) {
                    // ignored
                }
                in = null;
            }
            savedPassword = password;
            while (true) {
                try {
                    password = decode(component, toDecode);
                    return password;
                } catch (Exception e) {
                    throw new MasterPasswordServiceNotAvailableException(e.getLocalizedMessage());
                }
            }
        } catch (FileNotFoundException ignored) {
            throw new MasterPasswordServiceNotAvailableException(ignored.getLocalizedMessage());
        }
    }

    public final String encode(final Component component, final char[] toEncode)
            throws MasterPasswordServiceNotAvailableException, Exception {
        if (savedPassword == null) {
            savedPassword = checkMasterPassword(component);
        }
        return encode(savedPassword, toEncode);
    }

    private MasterPasswordService(Component component) {

    }

    public static synchronized MasterPasswordService getInstance(Component component) {
        if (instance == null) {
            instance = new MasterPasswordService(component);
        }
        return instance;
    }

    private static final String algorithm = "Blowfish/CBC/PKCS5Padding"; //$NON-NLS-1$

    private final static SecureRandom secureRandom;
    static {
        SecureRandom sec;
        try {
            sec = SecureRandom.getInstance("SHA1PRNG"); //$NON-NLS-1$
        } catch (NoSuchAlgorithmException e) {
            sec = new SecureRandom();
        }
        secureRandom = sec;
    }

    private static final String encode(final char[] password, final char[] toEncode) throws InvalidKeyException,
            Exception {
        byte salt[] = new byte[32];
        secureRandom.nextBytes(salt);
        SecretKey key = generateKey(salt, password);
        final Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        final String iv = Base64Codec.encode(cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV());
        byte[] d = cipher.doFinal(new String(toEncode).getBytes(CHARSET));
        String data = Base64Codec.encode(d);
        return Base64Codec.encode(salt) + separator + iv + separator + data;
    }

    private final static SecretKey generateKey(final byte salt[], final char[] password) {
        try {

            final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); //$NON-NLS-1$
            final KeySpec spec = new PBEKeySpec(password, salt, 65536, 56);
            final SecretKey tmp = factory.generateSecret(spec);
            final SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "Blowfish"); //$NON-NLS-1$
            return secret;
        } catch (Exception e) {
            throw new IllegalStateException("Key cant be generated !"); //$NON-NLS-1$
        }
    }

    private final static String separator = "::"; //$NON-NLS-1$

    private static final char[] decode(final char password[], final String decode) throws InvalidKeyException,
            BadPaddingException, Exception {
        final String[] vals = decode.split(separator);
        final byte[] salt = Base64Codec.decode(vals[0]);
        final byte[] iv = Base64Codec.decode(vals[1]);
        final byte[] encodedPass = Base64Codec.decode(vals[2]);
        final Cipher cipher = Cipher.getInstance(algorithm);
        SecretKey key = generateKey(salt, password);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return new String(cipher.doFinal(encodedPass), CHARSET).toCharArray();
    }

    private char[] savedPassword;

    public char[] decode(Component component, final String toDecode) throws MasterPasswordServiceNotAvailableException {
        char password[];
        final JPasswordField p = new JPasswordField();
        JPanel px = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.ipady = 5;
        JLabel label = new JLabel(Messages.getString("pleaseEnterMasterPasswordToUnlock")); //$NON-NLS-1$
        px.add(label, c);
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.BASELINE_TRAILING;
        c.ipadx = 5;
        c.gridx = 0;
        c.fill = GridBagConstraints.NONE;
        {
            JLabel lbl = new JLabel(Messages.getString("password")); //$NON-NLS-1$
            lbl.setLabelFor(p);
            px.add(lbl, c);
            c.gridy++;
        }
        c.gridy = 1;
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        c.weightx = 1;
        px.add(p, c);
        c.gridy++;
        boolean tryPreviousPassword = savedPassword != null;
        password = savedPassword;
        do {
            if (!tryPreviousPassword) {
                Thread tx = new Thread() {

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                p.requestFocusInWindow();
                            }
                        });
                    }
                };
                tx.start();
                int ret = JOptionPane.showConfirmDialog(component, px, Messages.getString("pleaseEnterMasterPassword"), //$NON-NLS-1$
                                                        JOptionPane.OK_CANCEL_OPTION);
                password = p.getPassword();
                if (ret != JOptionPane.OK_OPTION) {
                    throw new MasterPasswordServiceNotAvailableException(Messages.getString("noMasterPassword")); //$NON-NLS-1$
                }
            }
            tryPreviousPassword = false;
            try {
                char[] data = decode(password, toDecode);
                savedPassword = password;
                return data;
            } catch (InvalidKeyException e) {
                label.setText(Messages.getString("passwordIsIncorrect")); //$NON-NLS-1$
            } catch (BadPaddingException err) {
                label.setText(Messages.getString("passwordIsIncorrect")); //$NON-NLS-1$
            } catch (Exception err) {
                err.printStackTrace();
            }
        } while (true);
    }
}
