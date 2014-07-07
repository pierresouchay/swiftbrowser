#!/bin/bash
 
# make sure we are in the correct dir when we double-click a .command file
 
# set up your app name, version number, and background image file name
APP_NAME="SwiftBrowser"
VERSION="$1"
DMG_BACKGROUND_IMG="background.png"
 
VOL_NAME="${APP_NAME} ${VERSION}"   # volume name will be "SuperCoolApp 1.0.0"
DMG_TMP="${VOL_NAME}-temp.dmg"
DMG_FINAL="${VOL_NAME}.dmg"         # final DMG name will be "SuperCoolApp 1.0.0.dmg"
STAGING_DIR="./Install"             # we copy all our stuff into this dir

# clear out any old data
rm -rf "${STAGING_DIR}" "${DMG_TMP}" "${DMG_FINAL}"
 
# copy over the stuff we want in the final disk image to our staging dir
mkdir -p "${STAGING_DIR}"
cp -rpf "swiftbrowser.app" "${STAGING_DIR}/$APP_NAME.app"
cp README.txt "${STAGING_DIR}/README.txt"
cp "System Settings.command" "${STAGING_DIR}/System Settings.command"

# figure out how big our DMG needs to be
#  assumes our contents are at least 1M!
du -sh "${STAGING_DIR}"
SIZE=`du -sh "${STAGING_DIR}" | sed 's/\([0-9]*\)M\(.*\)/\1/' | sed 's/,/./'`
if [ -n $SIZE ]
then
echo "size=$SIZE"
else
SIZE=0
fi

SIZE=`echo "${SIZE} + 1.0" | bc | awk '{print int($1+0.5)}'`
echo $SIZE
 
if [ $? -ne 0 ]; then
   echo "Error: Cannot compute size of staging dir"
   exit
fi
 
# create the temp DMG file
hdiutil create -srcfolder "${STAGING_DIR}" -volname "${VOL_NAME}" -fs HFS+ \
      -fsargs "-c c=64,a=16,e=16" -format UDRW -size ${SIZE}M "${DMG_TMP}"
 
echo "Created DMG: ${DMG_TMP}"
 
# mount it and save the device
DEVICE=$(hdiutil attach -readwrite -noverify "${DMG_TMP}" | \
         egrep '^/dev/' | sed 1q | awk '{print $1}')
 
sleep 3
#cp README.txt /Volumes/"${VOL_NAME}/"
# add a link to the Applications dir
echo "Add link to /Applications"
pushd /Volumes/"${VOL_NAME}"
ln -s /Applications
popd
 
# add a background image
mkdir /Volumes/"${VOL_NAME}"/.background
cp "${DMG_BACKGROUND_IMG}" /Volumes/"${VOL_NAME}"/.background/
 
# tell the Finder to resize the window, set the background,
#  change the icon size, place the icons in the right position, etc.
echo '
   tell application "Finder"
     tell disk "'${VOL_NAME}'"
           open
           set current view of container window to icon view
           set toolbar visible of container window to false
           set statusbar visible of container window to false
           set the bounds of container window to {100, 100, 740, 420}
           set viewOptions to the icon view options of container window
           set arrangement of viewOptions to not arranged
           set icon size of viewOptions to 72
           set background picture of viewOptions to file ".background:'${DMG_BACKGROUND_IMG}'"
           set position of item "'${APP_NAME}'.app" of container window to {160, 190}
           set position of item "Applications" of container window to {400, 190}
           set position of item "README.txt" of container window to {540, 190}
           set position of item "System Settings.command" of container window to {540, 100}
           close
           open
           update without registering applications
           delay 2
     end tell
   end tell
' | osascript
 
sync
sleep 1

# unmount it
hdiutil detach "${DEVICE}"
 
# now make the final image a compressed disk image
echo "Creating compressed image"
hdiutil convert "${DMG_TMP}" -format UDZO -imagekey zlib-level=9 -o "${DMG_FINAL}"
 
# clean up
rm -rf "${DMG_TMP}"
rm -rf "${STAGING_DIR}"
 
echo 'Done.'
 
exit
