#!/bin/sh
if [ $# -lt 1 ]
then
	echo "USAGE $0 VERSION"
	exit 1
fi

PACKAGE_NAME="swiftbrowser"
PACKAGE_VERSION="$1"
SOURCE_DIR=$PWD
TEMP_DIR="/tmp"
mkdir -p $TEMP_DIR/debian/DEBIAN
mkdir -p $TEMP_DIR/debian/usr/lib
mkdir -p $TEMP_DIR/debian/usr/bin
mkdir -p $TEMP_DIR/debian/usr/share/applications
mkdir -p $TEMP_DIR/debian/usr/share/icons/hicolor/64x64
mkdir -p $TEMP_DIR/debian/usr/share/doc/$PACKAGE_NAME
echo "Package: $PACKAGE_NAME" > $TEMP_DIR/debian/DEBIAN/control
echo "Version: $PACKAGE_VERSION" >> $TEMP_DIR/debian/DEBIAN/control
cat debian/control >> $TEMP_DIR/debian/DEBIAN/control
cp ../target/swift-browser-$PACKAGE_VERSION-bin.jar $TEMP_DIR/debian/usr/lib/swiftbrowser.jar
cp debian/swiftbrowser.png $TEMP_DIR/debian/usr/share/icons/hicolor/64x64
cp debian/swiftbrowser.desktop $TEMP_DIR/debian/usr/share/applications/
cp debian/copyright $TEMP_DIR/debian/usr/share/doc/$PACKAGE_NAME/
grep '<td>' ../index.html |sed 's/<tr><td>\(.*\)<\/td><td>\(.*\)<\/td><td>\(.*\)<\/td><\/tr>/\1 - \2 - \3/g' |sed 's/<br\/>/ \/\/ /g' > $TEMP_DIR/debian/usr/share/doc/$PACKAGE_NAME/changelog.gz
echo '#!/bin/sh' > $TEMP_DIR/debian/usr/bin/$PACKAGE_NAME
echo "java -jar /usr/lib/swiftbrowser.jar" >> $TEMP_DIR/debian/usr/bin/$PACKAGE_NAME
chmod 755 $TEMP_DIR/debian/usr/bin/$PACKAGE_NAME
PACKAGE_SIZE=`du -bs $TEMP_DIR/debian | cut -f 1`
PACKAGE_SIZE=$((PACKAGE_SIZE/1024))
echo "Installed-Size: $PACKAGE_SIZE" >> $TEMP_DIR/debian/DEBIAN/control
chown -R root $TEMP_DIR/debian/
chgrp -R root $TEMP_DIR/debian/
chmod -R a+r $TEMP_DIR/debian
cd $TEMP_DIR/
dpkg --build debian
mv debian.deb $SOURCE_DIR/$PACKAGE_NAME-$PACKAGE_VERSION.deb
rm -r $TEMP_DIR/debian
