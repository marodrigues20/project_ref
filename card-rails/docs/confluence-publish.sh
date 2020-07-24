SERVICE_PATH=$1
ANCESTOR_ID="57499075"
PAGE_TITLE_PREFIX="Card Rails - "

echo "Publishing ascii docs on folder: ${SERVICE_PATH}/docs"
echo "Using ancestor id: $ANCESTOR_ID"
echo "Using page title prefix: $PAGE_TITLE_PREFIX"

if [ ! -d "$SERVICE_PATH" ]
then
 echo "SERVICE path does not exist: ${SERVICE_PATH}"
 exit 1
fi

if [ -z "$CONFLUENCE_USERNAME" ]
then
 echo "CONFLUENCE_USERNAME environment variable does not exist"
 exit 2
else
 echo "Using confluence username: $CONFLUENCE_USERNAME"
fi

if [ -z "$CONFLUENCE_PASSWORD" ]
then
 echo "CONFLUENCE_PASSWORD environment variable does not exist"
 exit 3
else
 echo "Using confluence password: $CONFLUENCE_PASSWORD"
fi

docker run --rm -e ROOT_CONFLUENCE_URL=https://confluence.10x.mylti3gh7p4x.net -e USERNAME=${CONFLUENCE_USERNAME} -e PASSWORD=${CONFLUENCE_PASSWORD} -e SPACE_KEY=SM -e ANCESTOR_ID=${ANCESTOR_ID} -e PAGE_TITLE_PREFIX="${PAGE_TITLE_PREFIX}" -v ${SERVICE_PATH}/docs:/var/asciidoc-root-folder confluencepublisher/confluence-publisher:0.0.0-SNAPSHOT
