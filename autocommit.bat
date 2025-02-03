@REM Save current branch to a variable

for /f "tokens=*" %%a in ('git branch --show-current') do set current_branch=%%a

@REM Add all files to the index, cool change

git add .

@REM Commit the changes

git commit -m "Auto commit"

@REM Push the changes to the remote repository

git push origin %current_branch%