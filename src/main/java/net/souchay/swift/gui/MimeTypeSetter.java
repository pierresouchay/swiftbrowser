/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-06-17 16:43:28 +0200 (Mar 17 jui 2014) $
 */
package net.souchay.swift.gui;

import java.io.File;
import java.util.HashMap;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3820 $
 * 
 */
@SuppressWarnings("nls")
public class MimeTypeSetter {

    public final static HashMap<String, String> mimes = new HashMap<String, String>();
    static {
        mimes.put("7z", "application/x-7z-compressed");
        mimes.put("odt", "application/vnd.oasis.opendocument.text");
        mimes.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
        mimes.put("odp", "application/vnd.oasis.opendocument.presentation");
        mimes.put("odg", "application/vnd.oasis.opendocument.graphics");
        mimes.put("odc", "application/vnd.oasis.opendocument.chart");
        mimes.put("odf", "application/vnd.oasis.opendocument.formula");
        mimes.put("odi", "application/vnd.oasis.opendocument.image");
        mimes.put("odm", "application/vnd.oasis.opendocument.text-master");
        mimes.put("odb", "application/vnd.sun.xml.base");
        mimes.put("odb", "application/vnd.oasis.opendocument.base");
        mimes.put("odb", "application/vnd.oasis.opendocument.database");
        mimes.put("ott", "application/vnd.oasis.opendocument.text-template");
        mimes.put("ots", "application/vnd.oasis.opendocument.spreadsheet-template");
        mimes.put("otp", "application/vnd.oasis.opendocument.presentation-template");
        mimes.put("otg", "application/vnd.oasis.opendocument.graphics-template");
        mimes.put("otc", "application/vnd.oasis.opendocument.chart-template");
        mimes.put("otf", "application/vnd.oasis.opendocument.formula-template");
        mimes.put("oti", "application/vnd.oasis.opendocument.image-template");
        mimes.put("oth", "application/vnd.oasis.opendocument.text-web");
        mimes.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        mimes.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
        mimes.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
        mimes.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        mimes.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        mimes.put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
        mimes.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mimes.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
        mimes.put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
        mimes.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
        mimes.put("3g2", "video/3gpp2");
        mimes.put("3gp", "video/3gpp");
        mimes.put("3gpp", "video/3gpp");
        mimes.put("3gpp2", "video/3gpp2");
        mimes.put("aac", "audio/aac");
        mimes.put("acx", "application/internet-property-stream");
        mimes.put("ai", "application/postscript");
        mimes.put("aif", "audio/x-aiff");
        mimes.put("aifc", "audio/x-aiff");
        mimes.put("aiff", "audio/x-aiff");
        mimes.put("asf", "video/x-ms-asf");
        mimes.put("asr", "video/x-ms-asf");
        mimes.put("asx", "video/x-ms-asf");
        mimes.put("au", "audio/basic");
        mimes.put("avi", "video/x-msvideo");
        mimes.put("axs", "application/olescript");
        mimes.put("bas", "text/plain");
        mimes.put("bcpio", "application/x-bcpio");
        mimes.put("bin", "application/octet-stream");
        mimes.put("bmp", "image/bmp");
        mimes.put("c", "text/plain");
        mimes.put("cat", "application/vnd.ms-pkiseccat");
        mimes.put("cdf", "application/x-cdf");
        mimes.put("cdf", "application/x-netcdf");
        mimes.put("cer", "application/x-x509-ca-cert");
        mimes.put("class", "application/octet-stream");
        mimes.put("clp", "application/x-msclip");
        mimes.put("cmx", "image/x-cmx");
        mimes.put("cod", "image/cis-cod");
        mimes.put("cpio", "application/x-cpio");
        mimes.put("crd", "application/x-mscardfile");
        mimes.put("crl", "application/pkix-crl");
        mimes.put("crt", "application/x-x509-ca-cert");
        mimes.put("csh", "application/x-csh");
        mimes.put("css", "text/css");
        mimes.put("csv", "text/csv");
        mimes.put("dcr", "application/x-director");
        mimes.put("der", "application/x-x509-ca-cert");
        mimes.put("dir", "application/x-director");
        mimes.put("dll", "application/x-msdownload");
        mimes.put("dmg", "application/x-apple-diskimage");
        mimes.put("dms", "application/octet-stream");
        mimes.put("doc", "application/msword");
        mimes.put("dot", "application/msword");
        mimes.put("dvi", "application/x-dvi");
        mimes.put("dxr", "application/x-director");
        mimes.put("eps", "application/postscript");
        mimes.put("etx", "text/x-setext");
        mimes.put("evy", "application/envoy");
        mimes.put("exe", "application/octet-stream");
        mimes.put("fif", "application/fractals");
        mimes.put("flr", "x-world/x-vrml");
        mimes.put("gif", "image/gif");
        mimes.put("gtar", "application/x-gtar");
        mimes.put("gz", "application/x-gzip");
        mimes.put("h", "text/plain");
        mimes.put("hdf", "application/x-hdf");
        mimes.put("hlp", "application/winhlp");
        mimes.put("hqx", "application/mac-binhex40");
        mimes.put("hta", "application/hta");
        mimes.put("htc", "text/x-component");
        mimes.put("htm", "text/html");
        mimes.put("html", "text/html");
        mimes.put("htt", "text/webviewhtml");
        mimes.put("ico", "image/x-icon");
        mimes.put("ief", "image/ief");
        mimes.put("iii", "application/x-iphone");
        mimes.put("ins", "application/x-internet-signup");
        mimes.put("isp", "application/x-internet-signup");
        mimes.put("jfif", "image/pipeg");
        mimes.put("jpe", "image/jpeg");
        mimes.put("jnlp", "application/x-java-jnlp-file");
        mimes.put("jpeg", "image/jpeg");
        mimes.put("jpg", "image/jpeg");
        mimes.put("png", "image/png");
        mimes.put("ics", "text/calendar");
        mimes.put("psd", "image/vnd.adobe.photoshop");
        mimes.put("js", "application/x-javascript");
        mimes.put("latex", "application/x-latex");
        mimes.put("lha", "application/octet-stream");
        mimes.put("cbz", "application/x-cbr");
        mimes.put("lsf", "video/x-la-asf");
        mimes.put("lsx", "video/x-la-asf");
        mimes.put("lzh", "application/octet-stream");
        mimes.put("m13", "application/x-msmediaview");
        mimes.put("m14", "application/x-msmediaview");
        mimes.put("m3u", "audio/x-mpegurl");
        mimes.put("m4a", "audio/mp4");
        mimes.put("man", "application/x-troff-man");
        mimes.put("mdb", "application/x-msaccess");
        mimes.put("me", "application/x-troff-me");
        mimes.put("mht", "message/rfc822");
        mimes.put("mhtml", "message/rfc822");
        mimes.put("mid", "audio/mid");
        mimes.put("mkv", "video/x-matroska");
        mimes.put("mny", "application/x-msmoney");
        mimes.put("mov", "video/quicktime");
        mimes.put("movie", "video/x-sgi-movie");
        mimes.put("mp2", "video/mpeg");
        mimes.put("mp3", "audio/mpeg");
        mimes.put("mpa", "video/mpeg");
        mimes.put("mpe", "video/mpeg");
        mimes.put("mpeg", "video/mpeg");
        mimes.put("mpg", "video/mpeg");
        mimes.put("mpp", "application/vnd.ms-project");
        mimes.put("mpv2", "video/mpeg");
        mimes.put("ms", "application/x-troff-ms");
        mimes.put("msg", "application/vnd.ms-outlook");
        mimes.put("mvb", "application/x-msmediaview");
        mimes.put("nc", "application/x-netcdf");
        mimes.put("nws", "message/rfc822");
        mimes.put("oda", "application/oda");
        mimes.put("p10", "application/pkcs10");
        mimes.put("p12", "application/x-pkcs12");
        mimes.put("p7b", "application/x-pkcs7-certificates");
        mimes.put("p7c", "application/x-pkcs7-mime");
        mimes.put("p7m", "application/x-pkcs7-mime");
        mimes.put("p7r", "application/x-pkcs7-certreqresp");
        mimes.put("p7s", "application/x-pkcs7-signature");
        mimes.put("pbm", "image/x-portable-bitmap");
        mimes.put("pdf", "application/pdf");
        mimes.put("pfx", "application/x-pkcs12");
        mimes.put("pgm", "image/x-portable-graymap");
        mimes.put("pko", "application/ynd.ms-pkipko");
        mimes.put("pma", "application/x-perfmon");
        mimes.put("pmc", "application/x-perfmon");
        mimes.put("pml", "application/x-perfmon");
        mimes.put("pmr", "application/x-perfmon");
        mimes.put("pmw", "application/x-perfmon");
        mimes.put("pnm", "image/x-portable-anymap");
        mimes.put("pot", "application/vnd.ms-powerpoint");
        mimes.put("ppm", "image/x-portable-pixmap");
        mimes.put("pps", "application/vnd.ms-powerpoint");
        mimes.put("ppt", "application/vnd.ms-powerpoint");
        mimes.put("prf", "application/pics-rules");
        mimes.put("ps", "application/postscript");
        mimes.put("pub", "application/x-mspublisher");
        mimes.put("qt", "video/quicktime");
        mimes.put("ra", "audio/x-pn-realaudio");
        mimes.put("ram", "audio/x-pn-realaudio");
        mimes.put("rar", "application/x-rar-compressed");
        mimes.put("ras", "image/x-cmu-raster");
        mimes.put("rgb", "image/x-rgb");
        mimes.put("rmi", "audio/mid");
        mimes.put("roff", "application/x-troff");
        mimes.put("rtf", "application/rtf");
        mimes.put("rtx", "text/richtext");
        mimes.put("scd", "application/x-msschedule");
        mimes.put("sct", "text/scriptlet");
        mimes.put("setpay", "application/set-payment-initiation");
        mimes.put("setreg", "application/set-registration-initiation");
        mimes.put("sh", "application/x-sh");
        mimes.put("shar", "application/x-shar");
        mimes.put("sit", "application/x-stuffit");
        mimes.put("snd", "audio/basic");
        mimes.put("spc", "application/x-pkcs7-certificates");
        mimes.put("spl", "application/futuresplash");
        mimes.put("src", "application/x-wais-source");
        mimes.put("sst", "application/vnd.ms-pkicertstore");
        mimes.put("stl", "application/vnd.ms-pkistl");
        mimes.put("stm", "text/html");
        mimes.put("sv4cpio", "application/x-sv4cpio");
        mimes.put("sv4crc", "application/x-sv4crc");
        mimes.put("svg", "image/svg+xml");
        mimes.put("swf", "application/x-shockwave-flash");
        mimes.put("t", "application/x-troff");
        mimes.put("tar", "application/x-tar");
        mimes.put("tcl", "application/x-tcl");
        mimes.put("tex", "application/x-tex");
        mimes.put("texi", "application/x-texinfo");
        mimes.put("texinfo", "application/x-texinfo");
        mimes.put("tgz", "application/x-compressed-tar");
        mimes.put("tif", "image/tiff");
        mimes.put("tiff", "image/tiff");
        mimes.put("tr", "application/x-troff");
        mimes.put("trm", "application/x-msterminal");
        mimes.put("tsv", "text/tab-separated-values");
        mimes.put("txt", "text/plain");
        mimes.put("ts", "video/mp2t");
        mimes.put("uls", "text/iuls");
        mimes.put("ustar", "application/x-ustar");
        mimes.put("vcf", "text/x-vcard");
        mimes.put("vrml", "x-world/x-vrml");
        mimes.put("wav", "audio/x-wav");
        mimes.put("wcm", "application/vnd.ms-works");
        mimes.put("wdb", "application/vnd.ms-works");
        mimes.put("wks", "application/vnd.ms-works");
        mimes.put("wmf", "application/x-msmetafile");
        mimes.put("wps", "application/vnd.ms-works");
        mimes.put("wri", "application/x-mswrite");
        mimes.put("wrl", "x-world/x-vrml");
        mimes.put("wrz", "x-world/x-vrml");
        mimes.put("xaf", "x-world/x-vrml");
        mimes.put("xbm", "image/x-xbitmap");
        mimes.put("xla", "application/vnd.ms-excel");
        mimes.put("xlc", "application/vnd.ms-excel");
        mimes.put("xlm", "application/vnd.ms-excel");
        mimes.put("xls", "application/vnd.ms-excel");
        mimes.put("xlt", "application/vnd.ms-excel");
        mimes.put("xlw", "application/vnd.ms-excel");
        mimes.put("xof", "x-world/x-vrml");
        mimes.put("xpm", "image/x-xpixmap");
        mimes.put("xwd", "image/x-xwindowdump");
        mimes.put("z", "application/x-compress");
        mimes.put("zip", "application/zip");
    }

    public final static String DEFAULT_MIME = "octet/stream";

    /**
     * Default Constructor
     */
    public MimeTypeSetter() {
    }

    /**
     * Get a mime type from file name
     * 
     * @param f
     * @return The mime-type
     */
    public String getMimeType(File f) {
        final String name = f.getName();
        final int idx = name.lastIndexOf(".");
        if (idx < 1 || (idx > name.length() - 1))
            return DEFAULT_MIME;
        final String ext = name.substring(idx + 1).toLowerCase();
        if (ext.length() > 5)
            return DEFAULT_MIME;
        final String mime = mimes.get(ext);
        if (mime == null)
            return DEFAULT_MIME;
        return mime;
    }
}
