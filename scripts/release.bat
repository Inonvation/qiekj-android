@echo off
cd /d F:\qiekj-android
git add -A
git commit -F .git\COMMIT_MSG.txt
git push
git tag v0.1.0 main
git push --tags
echo Release v0.1.0 pushed
