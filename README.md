# Compare File

This app use for comparing between two file

## Arguments
1. path to 1st file (required)
2. path to 2nd file (required)
3. output directory (required)
4. config file (optional)

## Result files
1. List of only left files (onlyLeft.txt)
2. List of only right files (onlyRight.txt)
3. Different of files (diff.txt)
4. Different by fields (diffDetail.txt)

## Example Files

You can find example file at `/example` of this repository

## Example Execution Command

```
java -cp "CompareFile-1.0-SNAPSHOT.jar" com.poc.comparefile.CompareFile left.txt right.txt output config.properties
```