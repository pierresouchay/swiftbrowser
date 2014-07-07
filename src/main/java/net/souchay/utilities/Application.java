/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-07-07 12:12:06 +0200 (Lun 07 jul 2014) $
 */
package net.souchay.utilities;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * General portable application code
 * 
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3835 $
 * 
 */
public class Application {

    public static Boolean macOs = null;

    private static Object theApplicationObject;

    /**
     * Set the Application for Java with full compatibility with Apple.
     * 
     * Call this as first call in you main
     * 
     * @param name The name of application
     * @param isVisualApplication true is application has a GUI
     */
    public static void setApplicationName(final String name, boolean isVisualApplication) {
        System.setProperty("visualvm.display.name", name); //$NON-NLS-1$
        if (isVisualApplication) {
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false"); //$NON-NLS-1$ //$NON-NLS-2$
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", name); //$NON-NLS-1$
            System.setProperty("com.apple.macos.useScreenMenuBar", "true"); //$NON-NLS-1$ //$NON-NLS-2$
            System.setProperty("apple.laf.useScreenMenuBar", "true"); //$NON-NLS-1$ //$NON-NLS-2$
            // Avoid issues with Drag And Drop and Java 7
            //System.setProperty("java.util.Arrays.useLegacyMergeSort", "true"); //$NON-NLS-1$//$NON-NLS-2$
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                // UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.setProperty("java.awt.headless", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static Method setDockIconBadge;

    public static void setDockIconBadge(final String value) {
        if (setDockIconBadge != null) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    try {
                        setDockIconBadge.invoke(theApplicationObject, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    /**
     * @copyright Pierre Souchay - 2013,2014
     * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
     * @version $Revision: 3835 $
     * 
     */
    public static class ApplicationConfiguration {

        /**
         * @param handler
         * @param handlePreferrences
         * @param dockImage
         */
        public ApplicationConfiguration(MacOSXHandle handler, boolean handlePreferrences, Image dockImage) {
            super();
            this.handler = handler;
            this.handlePreferrences = handlePreferrences;
            this.dockImage = dockImage;
        }

        private final MacOSXHandle handler;

        private final boolean handlePreferrences;

        private final Image dockImage;

        /**
         * get the handler
         * 
         * @return the handler
         */
        public MacOSXHandle getHandler() {
            return handler;
        }

        /**
         * get the handlePreferrences
         * 
         * @return the handlePreferrences
         */
        public boolean isHandlePreferrences() {
            return handlePreferrences;
        }

        /**
         * get the dockImage
         * 
         * @return the dockImage
         */
        public Image getDockImage() {
            return dockImage;
        }
    }

    /**
     * This method registers a MacOSXHandle in order to perform operations
     * 
     * In order to use your own callbacks, please re-implement all the handle* methods
     * 
     * @param applicationConfiguration All the parameters applied
     * @return true if we are on Mac and did register the callbacks
     * 
     */
    public static boolean registerOSXApplicationMenu(ApplicationConfiguration applicationConfiguration) {

        try {
            ClassLoader cl = Application.class.getClassLoader();

            // Create class-objects for the classes and interfaces we need
            final Class<?> comAppleEawtApplicationClass = cl.loadClass("com.apple.eawt.Application"); //$NON-NLS-1$
            final Class<?> comAppleEawtApplicationListenerInterface = cl.loadClass("com.apple.eawt.ApplicationListener"); //$NON-NLS-1$

            // Create applicationlistener proxy
            Object applicationListenerProxy = Proxy.newProxyInstance(cl,
                                                                     new Class[] { comAppleEawtApplicationListenerInterface },
                                                                     applicationConfiguration.getHandler());

            // Get a real Application-object
            Method applicationGetApplicationMethod = comAppleEawtApplicationClass.getMethod("getApplication", //$NON-NLS-1$
                                                                                            new Class[0]);
            Object theApplicationObject = applicationGetApplicationMethod.invoke(null, new Object[0]);

            // Add the proxy application object as listener
            Method addApplicationListenerMethod = comAppleEawtApplicationClass.getMethod("addApplicationListener", //$NON-NLS-1$
                                                                                         new Class[] { comAppleEawtApplicationListenerInterface });

            if (applicationConfiguration.isHandlePreferrences()) {
                Method handlePm = comAppleEawtApplicationClass.getMethod("setEnabledPreferencesMenu", //$NON-NLS-1$
                                                                         new Class[] { boolean.class });
                handlePm.invoke(theApplicationObject, new Object[] { Boolean.TRUE });
            }

            Image image = applicationConfiguration.getDockImage();
            if (image != null) {
                Method setDockIconImage = theApplicationObject.getClass().getMethod("setDockIconImage", Image.class); //$NON-NLS-1$

                try {
                    setDockIconImage.invoke(theApplicationObject, image);
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {
                }
            }
            try {
                Application.setDockIconBadge = theApplicationObject.getClass()
                                                                   .getMethod("setDockIconBadge", String.class); //$NON-NLS-1$
                Application.theApplicationObject = theApplicationObject;
            } catch (Exception e) {
            }

            addApplicationListenerMethod.invoke(theApplicationObject, new Object[] { applicationListenerProxy });
            macOs = Boolean.TRUE;
            return true;

        } catch (Exception e) {
            macOs = Boolean.FALSE;
            return false;
        }
    }

    /**
     * True if system is Mac OS
     * 
     * @return
     */
    public final static boolean isMacOs() {
        if (macOs == null) {
            macOs = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return macOs.booleanValue();
    }

    /**
     * Tells whether to use meta or control for shortcuts
     * 
     * @return
     */
    public final static int getMetaOrControl() {
        return isMacOs() ? KeyEvent.META_DOWN_MASK : KeyEvent.CTRL_MASK;
    }

    /**
     * Class to re-implement to add your own callbacks
     * 
     * @copyright Pierre Souchay - 2013,2014
     * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
     * @version $Revision: 3835 $
     * 
     */
    public static class MacOSXHandle implements InvocationHandler {

        /**
         * @copyright Pierre Souchay - 2013,2014
         * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
         * @version $Revision: 3835 $
         * 
         */
        public static enum OPEN_TYPE {
            /**
             * Application opens
             */
            NEW_APP,
            /**
             * Application is already open, ask to open a new file
             */
            OPEN_FILE,
            /**
             * Application should print given file
             */
            PRINT_FILE
        }

        /**
         * Called when the user selects the About item in the application menu. Re-implement this to display your own
         * About page
         * 
         * @return true if you handled it
         */
        public boolean handleAbout() {
            return false;
        }

        /**
         * Called when the application receives an Open Application event from the Finder or another application.
         * 
         * @param applicationEvent
         * @return true if managed
         * @see #handleOpenFileAsString(OPEN_TYPE, String)
         * 
         */
        public final boolean handleOpenApplication(Object applicationEvent) {
            _handleOpen(OPEN_TYPE.NEW_APP, applicationEvent);
            return false;
        }

        /**
         * Called when the application receives an Open Document event from the Finder or another application.
         * 
         * @param openType what kind of operation is requested ?
         * @param fileToOpen
         * @return true if managed
         */
        public boolean handleOpenFileAsString(OPEN_TYPE openType, String fileToOpen) {
            return false;
        }

        private final boolean _handleOpen(final OPEN_TYPE openType, Object applicationEvent) {
            try {
                ClassLoader cl = Application.class.getClassLoader();
                final String getFilename = "getFilename"; //$NON-NLS-1$
                // Create class-objects for the classes and interfaces we need
                final Class<?> comAppleEawtApplicationEventClass = cl.loadClass("com.apple.eawt.ApplicationEvent"); //$NON-NLS-1$
                Method getFilenameMethod = comAppleEawtApplicationEventClass.getMethod(getFilename, new Class[0]);

                Object ret = getFilenameMethod.invoke(applicationEvent);
                if (ret instanceof String) {
                    // OK, this is a String
                    return handleOpenFileAsString(openType, (String) ret);
                } else {
                    //System.err.println(getFilename + " did not return a String, was=" + ret); //$NON-NLS-1$
                    return false;
                }
            } catch (Throwable e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * Called by Finder
         * 
         * @param applicationEvent
         * @return true if file was opened
         * @see #handleOpenFileAsString(OPEN_TYPE, String)
         */
        public final boolean handleOpenFile(Object applicationEvent) {
            return _handleOpen(OPEN_TYPE.OPEN_FILE, applicationEvent);
        }

        /**
         * Called when the Preference item in the application menu is selected.
         * 
         * @return true if managed
         */
        public boolean handlePreferences() {
            return false;
        }

        /**
         * Called when the application is sent a request to print a particular file or files.
         * 
         * @param applicationEvent The file to print
         * @return true if managed
         */
        public final boolean handlePrintFile(Object applicationEvent) {
            return _handleOpen(OPEN_TYPE.PRINT_FILE, applicationEvent);
        }

        /**
         * Called when the application is sent the Quit event.
         * 
         * @return true if managed
         */
        public boolean handleQuit() {
            System.exit(0);
            return true;
        }

        /**
         * Re-open the application
         * 
         * @return true if managed
         */
        public boolean handleReOpenApplication() {
            return false;
        }

        private Method applicationEventSetHandledMethod;

        /**
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method,
         *      java.lang.Object[])
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (applicationEventSetHandledMethod == null) {
                ClassLoader cl = getClass().getClassLoader();
                // Create class-objects for the classes and interfaces we need
                final Class<?> comAppleEawtApplicationEventClass = cl.loadClass("com.apple.eawt.ApplicationEvent"); //$NON-NLS-1$
                applicationEventSetHandledMethod = comAppleEawtApplicationEventClass.getMethod("setHandled", //$NON-NLS-1$
                                                                                               new Class[] { boolean.class });
            }
            final String m = method.getName();

            if ("handleAbout".equals(m)) { //$NON-NLS-1$
                applicationEventSetHandledMethod.invoke(args[0], new Object[] { handleAbout() });
            } else if ("handleOpenApplication".equals(m)) { //$NON-NLS-1$
                applicationEventSetHandledMethod.invoke(args[0], new Object[] { handleOpenApplication(args[0]) });
            } else if ("handleOpenFile".equals(m)) { //$NON-NLS-1$
                applicationEventSetHandledMethod.invoke(args[0], new Object[] { handleOpenFile(args[0]) });
            } else if ("handlePrintFile".equals(m)) { //$NON-NLS-1$
                applicationEventSetHandledMethod.invoke(args[0],
                                                        new Object[] { handlePrintFile(String.valueOf(args[0])) });
            } else if ("handleQuit".equals(m)) { //$NON-NLS-1$
                applicationEventSetHandledMethod.invoke(args[0], new Object[] { handleQuit() });
            } else if ("handlePreferences".equals(m)) { //$NON-NLS-1$
                applicationEventSetHandledMethod.invoke(args[0], new Object[] { handlePreferences() });
            } else if ("handleReOpenApplication".equals(m)) { //$NON-NLS-1$
                applicationEventSetHandledMethod.invoke(args[0], new Object[] { handleReOpenApplication() });
            } else {
                System.err.println("APPLE: Don't know how to handle " + m); //$NON-NLS-1$
            }
            return null;
        }
    }

    // @SuppressWarnings("unchecked")
    // public static <T> Collection<Future<T>> waitFromSwing(Component c, String msg, Collection<Future<T>> waitFor)
    // throws InterruptedException {
    // waitFromSwing(c, msg, waitFor.toArray(new Future[waitFor.size()]));
    // return waitFor;
    // }
    //
    // public static Window getWindow(Component c) {
    // if (c == null) {
    // return JOptionPane.getRootFrame();
    // } else if (c instanceof Window) {
    // return (Window) c;
    // } else {
    // return getWindow(c.getParent());
    // }
    // }
    //
    // private static boolean doWaitInSwing = false;
    //
    // /**
    // * Wait from Swing
    // *
    // * @param c
    // * @param msg
    // * @param toWaitFor
    // * @return
    // * @throws InterruptedException
    // */
    // public static <T> Future<T>[] waitFromSwing(final Component c, final String msg, final Future<T>... toWaitFor)
    // throws InterruptedException {
    // if (!doWaitInSwing)
    // return toWaitFor;
    // final SecondaryLoop secondaryLoop;
    // final Cursor jf;
    // if (SwingUtilities.isEventDispatchThread()) {
    // final EventQueue eventQueue = AccessController.doPrivileged(new PrivilegedAction<EventQueue>() {
    //
    // @Override
    // public EventQueue run() {
    // return Toolkit.getDefaultToolkit().getSystemEventQueue();
    // }
    // });
    // if (msg != null) {
    // // Window w = getWindow(c);
    // //
    // // jf.setModal(true);
    // // JProgressBar p = new JProgressBar();
    // // p.setIndeterminate(true);
    // // JPanel panel = new JPanel(new BorderLayout(10, 10));
    // // panel.add(p, BorderLayout.CENTER);
    // // jf.setContentPane(panel);
    // // jf.pack();
    // // jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    // // jf.setLocationByPlatform(true);
    // jf = c.getCursor();
    // c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    // } else {
    // jf = null;
    // }
    // secondaryLoop = eventQueue.createSecondaryLoop();
    //            Thread t = new Thread("SecondaryLoop") { //$NON-NLS-1$
    //
    // @Override
    // public void run() {
    // try {
    // final long start = System.currentTimeMillis();
    // boolean shown = false;
    // for (Future<?> toWait : toWaitFor) {
    // while (!toWait.isDone()) {
    // try {
    // toWait.get(100, TimeUnit.MILLISECONDS);
    // } catch (ExecutionException ignored) {
    // // ignored
    // } catch (InterruptedException err) {
    // return;
    // } catch (TimeoutException ignored) {
    // }
    // if (!shown && ((System.currentTimeMillis() - start) > 750)) {
    // shown = true;
    // if (jf != null) {
    // SwingUtilities.invokeLater(new Runnable() {
    //
    // @Override
    // public void run() {
    // // jf.set
    // }
    //
    // });
    //
    // }
    // }
    // }
    //
    // }
    // } finally {
    // if (secondaryLoop != null) {
    // secondaryLoop.exit();
    // }
    // if (jf != null) {
    // c.setCursor(Cursor.getDefaultCursor());
    // // jf.setVisible(false);
    // // jf.dispose();
    // }
    // }
    // }
    // };
    // t.start();
    // secondaryLoop.enter();
    //
    // } else {
    // secondaryLoop = null;
    // jf = null;
    // }
    // return toWaitFor;
    // }
}
