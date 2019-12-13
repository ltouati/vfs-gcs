echo "Deriving version using 'git describe'..."
git describe
export VERSION=$(git describe | sed 's/vfs-gcs-//g')
echo "Will publish version: ${VERSION}"

mvn deploy -DperformRelease=true -Drevision=${VERSION}
