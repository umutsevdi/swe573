# Git Version Control System
## Introduction

### What is a Version Control System?

A version control system (VCS) is software that records changes to files.
It lets individuals or teams to track history of modifications, allowing
them to compare, merge or revert changes.

Although many version control systems exist today (SVN, Mercurial), the most
popular by far the is Git.

### What is Git
Git is a distributed version‑control system designed to track changes in
files over time. Unlike older systems, every clone of a Git repository
contains the full history, so developers can work offline,
experiment freely, and later synchronize their work with others.

Git was born out of necessity. It was developed by Linus Torvalds, creator
of Linux to maintain the project. Git was released under an open source license.

Git’s internal model is a directed acyclic graph of commit objects where each
commit points to one or more parent commits. 

- **HEAD**: A reference to the current branch.
- **Commit**: A node containing a snapshot of the repository tree, author
and metadata.
- **Branch**: A movable reference that points to a commit. Performing a commit
moves the branch tip.

- `git init` Creates an empty repository.
- `git add <files>` Adds provided files to the staging area.
- `git commit` Creates a new node by moving the head while adding staged index.
- `git branch <name>` Creates a new reference pointing to the same commit.
- `git checkout/switch <branch>` Moves HEAD to the tip of chosen branch.
- `git merge <branch>` Using an available merging strategy compares tips of
branches. If the strategy can not complete merging, the graph keeps 
two incoming edges.
- `git rebase` Rewrites history by replaying a series of commits into a new
base. This is done by creating a new commit and writing changes to that commit
followed by swapping references. Original commits are orphaned unless they are
garbage collected.
- `git reset [option] <commit>` Moves the branch reference to selected commit.
- `git revert <commit>` Adds a new commit that inverts the recent commit.

In Git, every commit is immutable, and branches are merely pointers to commits.
