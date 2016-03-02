#!/bin/bash

cd /tmp
git clone https://${GH_OAUTH_TOKEN}@github.com/${GH_USER_NAME}/${GH_PROJECT_NAME} clonedir
mkdir ${GH_PROJECT_NAME}
cd ${GH_PROJECT_NAME}/
mkdir libs
cd ..
ls $TRAVIS_BUILD_DIR/build/libs/
cp $TRAVIS_BUILD_DIR/build/libs/${GH_PROJECT_NAME}-*.jar ${GH_PROJECT_NAME}/libs/
zip ${GH_PROJECT_NAME}.zip ${GH_PROJECT_NAME}/
cp ${GH_PROJECT_NAME}.zip clonedir/
cd clonedir
git config --global user.name $GIT_AUTHOR_NAME
git config --global user.email $GIT_AUTHOR_EMAIL
git add ${GH_PROJECT_NAME}.zip
git commit -m "Committed by Travis-CI"
git push https://${GH_OAUTH_TOKEN}@github.com/${GH_USER_NAME}/${GH_REPO_NAME} 2>&1