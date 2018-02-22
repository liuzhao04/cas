#!/bin/bash

branchName="master"
gradle="sudo ./gradlew"
gradleUpload="uploadArchives -x javadoc -x test -x check --parallel "
gradleUploadOptions="-DpublishSnapshots=true -DsonatypeUsername=${SONATYPE_USER} -DsonatypePassword=${SONATYPE_PWD}"

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "$branchName" ] && [ "$MATRIX_JOB_TYPE" == "SNAPSHOT" ]; then
    if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[skip snapshots]"* ]]; then
        echo -e "The build will skip deploying snapshot artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
    else
        echo -e "The build will deploy snapshot artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
        upload="$gradle $gradleUpload $gradleUploadOptions"
        echo $upload
        eval $upload
        retVal=$?
        echo -e "*************************************************************"
        echo -e "Deploying snapshots to Sonatype finished at `date` \n"
        echo -e "*************************************************************"
    fi
else
    echo -e "*************************************************************"
    echo -e "Publishing snapshots to Sonatype will be skipped.\n"
    echo -e "*************************************************************"
fi
