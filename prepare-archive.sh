#!/bin/bash
git config --global user.name $GIT_AUTHOR_NAME
git config --global user.email $GIT_AUTHOR_EMAIL

# Checkout Repo to deploy to
echo "\n1. Checkout Repo to deploy to"
cd /tmp
rm -rf clonedir
git clone https://${GH_OAUTH_TOKEN}@github.com/${GH_USER_NAME}/${GH_REPO_NAME} clonedir
cd clonedir
git reset
git pull https://${GH_OAUTH_TOKEN}@github.com/${GH_USER_NAME}/${GH_REPO_NAME}

# Create Directories for zip
echo "\n 2. Create Directories for zip"
cd /tmp
mkdir ${GH_PROJECT_NAME}
mkdir ${GH_PROJECT_NAME}/libs

# Take a look.
echo "\n 3. Take a look."
ls $TRAVIS_BUILD_DIR/build/libs
ls $TRAVIS_BUILD_DIR/libs/*.jar

# copy jars to directory
echo "\n 4. copy jars to directory."

cp -R $TRAVIS_BUILD_DIR/build/libs* ${GH_PROJECT_NAME}/libs/
cp -R $TRAVIS_BUILD_DIR/libs/*.jar ${GH_PROJECT_NAME}/libs/

# Take a look in our zip directory
echo "\n 5. Take a look in our zip directory:"
ls ${GH_PROJECT_NAME}/libs/

# Zip it
echo "\n 6. Zip it:"
zip ${GH_PROJECT_NAME}.zip ${GH_PROJECT_NAME}/*

# Go to clone we created earlier.
echo "\n 7. Go to clone we created earlier.:"
cd /tmp/clonedir

# Copy the new zip to the clone of the repo
echo "\n 8. Copy the new zip to the clone of the repo:"
cp ${GH_PROJECT_NAME}.zip .

# Take a look.
echo "\n 9. Take a look."
ls

# Add, commit, and push
echo "\n 10. Add, commit, and push:"
git add ${GH_PROJECT_NAME}.zip
git commit -a -m "Committed by Travis-CI"
git push https://${GH_OAUTH_TOKEN}@github.com/${GH_USER_NAME}/${GH_REPO_NAME} 2>&1