Summary: A teaching utily to graphically animate the stack and heap of
an java program execution.

Explanations:

This utility is a kind of debugger (a jdi client indeed !) where
the user can basically step-line, staying in the main class package and 
when gets a feedback: source display with highlighted current line and 
a graphic display for execution stack, class instances and array instances.

Variables, fields, array elements what are object or array references
are displayed as arrows and labels (object id based on object
hash). So stack and instances form an oriented graph.

Currently no nice auto layout is provided, but instances are draggable
and so user can correctly layout the instances (to get nice demos for
students for example).

Views are zoomable (+/- key) to allow uses as a demo or as
"observation" labs for very beginner students. A basic preferences
file keeps some user set-ups: windows last positions, last run class
name, last zoom level... Some other properties (colors, font
family,...) are editable (=preferences) using a provided, more than
basic "preferences" editor window.

License ? DWYW: Do  Whatever You Want !

Author: Laurent.Andrey@loria.fr



