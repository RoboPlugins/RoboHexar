#!/bin/bash
git config --global user.name $GIT_AUTHOR_NAME
git config --global user.email $GIT_AUTHOR_EMAIL

cd /tmp
rm -rf clonedir
git clone https://${GH_OAUTH_TOKEN}@github.com/${GH_USER_NAME}/${GH_REPO_NAME} clonedir
cd clonedir
git reset
git pull https://${GH_OAUTH_TOKEN}@github.com/${GH_USER_NAME}/${GH_REPO_NAME}
cd ..
mkdir ${GH_PROJECT_NAME}
mkdir ${GH_PROJECT_NAME}/libs
cp $TRAVIS_BUILD_DIR/build/libs/*.jar ${GH_PROJECT_NAME}/libs/
zip ${GH_PROJECT_NAME}.zip ${GH_PROJECT_NAME}/
cp ${GH_PROJECT_NAME}.zip clonedir/
cd clonedir
git add ${GH_PROJECT_NAME}.zip
git commit -a -m "Committed by Travis-CI"
git push https://${GH_OAUTH_TOKEN}@github.com/${GH_USER_NAME}/${GH_REPO_NAME} 2>&1