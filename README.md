# JavaLib_Common_VRML
The `JavaLib_Common_*` libraries contains all code snippets, classes and concepts, I want to reuse over more than one project.  
In `JavaLib_Common_VRML` contains some class for creating VRML97 files and depends on `JavaLib_Common_Geometry`.  

`JavaLib_Common_VRML` is an extract of `JavaLib_Common`.  
At first and for a long time I've used SVN for versioning and all these extracts were made via `SVN externals` (separate files or whole folders).
So all changes in the "extracted" files in the "extract" libraries were also made in the original files of `JavaLib_Common`.
The "extracted" files had the same full history as the original ones.  
Now in GIT there is no similar mechanism like `SVN externals`, as far as I know.
So I've decided to make reduced copies of `JavaLib_Common`.
All files in the "extract" libraries were removed from `JavaLib_Common` and vice versa,
that each file in all these libraries is exclusivly in only one library.  
As a result, `JavaLib_Common` contains the complete history of all files, but only the least used files are left in the current state.
All other files were moved into the "extract" libraries, but without their history.

### Usage / Development
The current state of this library is compatible with JAVA 17. But most of its code should be compatible with JAVA 8.  
This is an Eclipse project. `.classpath` and `.project` files are contained in the repository.  
`JavaLib_Common_VRML` depends on `JavaLib_Common_Geometry`. This dependency is made via "project import" in Eclipse.
If you want to use this library / clone this repository, you should clone `JavaLib_Common_Geometry` too and put it into the same Eclipse workspace.
