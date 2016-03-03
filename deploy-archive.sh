#!/bin/bash

git config --global user.name $GIT_AUTHOR_NAME
git config --global user.email $GIT_AUTHOR_EMAIL

# Checkout Repo to deploy to
echo "  1. Checkout Repo to deploy to "

# Cleanup tmp directory
cd /tmp
rm -rf clonedir
rm -rf ${GH_PROJECT_NAME}
rm ${GH_PROJECT_NAME}.zip

cd /tmp
git clone https://${GH_OAUTH_TOKEN}@${GH_REF} clonedir

# Create Directories for zip
echo "  2. Create Directories for zip"
mkdir -p /tmp/${GH_PROJECT_NAME}/libs

# Take a look.
echo "  3. Take a look for the jar files"
ls -la $TRAVIS_BUILD_DIR/build/libs

# copy jars to directory
echo " 4. copy jars to directory."
cp $TRAVIS_BUILD_DIR/build/libs/* /tmp/${GH_PROJECT_NAME}/libs
cp $TRAVIS_BUILD_DIR/libs/*.jar /tmp/${GH_PROJECT_NAME}/libs

# Take a look in our zip directory
echo "  5. Take a look in our jar directory:"
ls -la /tmp/${GH_PROJECT_NAME}/libs/

# Zip it UP!
echo "  6. Zip it:"
cd /tmp
zip -r ${GH_PROJECT_NAME}.zip ${GH_PROJECT_NAME}

# Take a look AT ZIP
echo "  7. Take a look at the zip"
cd /tmp
ls -la

#Leave if there is no zip.
if [ -f /tmp/${GH_PROJECT_NAME}.zip ];
then
    echo "ZIP FOUND"
else
    echo "ZIP NOT FOUND ~~~ ERROR"
    exit 1
fi

# Copy the new zip to the clone of the repo
echo "  8. Copy the new zip to the clone of the repo:"
cp /tmp/${GH_PROJECT_NAME}.zip /tmp/clonedir/

# Go to clone we created earlier.
echo "  9. Go to clone we created earlier.:"
cd /tmp/clonedir

# Take a look.
echo "  10. Take a look."
ls -la

# Add, commit, and push
echo "  11. Add, commit, and push:"
pwd
git add ${GH_PROJECT_NAME}.zip
git commit -a -m "Committed by Travis-CI"
git push https://${GH_OAUTH_TOKEN}@${GH_REF} 2>&1