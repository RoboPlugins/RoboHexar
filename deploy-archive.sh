#!/bin/bash

git config --global user.name $GIT_AUTHOR_NAME
git config --global user.email $GIT_AUTHOR_EMAIL

# Checkout Repo to deploy to
echo "  1. Checkout Repo to deploy to "

# Cleanup tmp directory
cd /tmp
rm -rf clonedir
rm -rf ${GH_PROJECT_NAME}
rm *.zip

cd /tmp
git clone https://${GH_OAUTH_TOKEN}@${GH_REF} clonedir
#
##Leave if there is no zip.
#if [ -f $TRAVIS_BUILD_DIR/build/disributions/${GH_PROJECT_NAME}*.zip ];
#then
#    echo "ZIP FOUND"
#else
#    echo "ZIP NOT FOUND ~~~ ERROR"
#    exit 1
#fi

cd $TRAVIS_BUILD_DIR/build/distributions/
distroFileName=$(ls -t -U | grep -m 1 "RoboHexar")
#distroFileName=$(ls -t "RoboHexar"*.zip | head -n 1)

# Copy the new zip to the clone of the repo
echo "  8. move the new zip to the clone of the repo:"
mv $TRAVIS_BUILD_DIR/build/distributions/$distroFileName /tmp/clonedir/${GH_PROJECT_NAME}.zip

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