# Conway's Game of Life

This is a ClojureScript implementation of Conway's Game of Life.

## Running it

1. Make sure you have [Leiningen](http://leiningen.org/) 2.x installed.

2. Compile the ClojureScript code to Javascript:

        $ lein cljsbuild once

3. Open `index.html` in your browser.

## Developing

It's convenient with auto-compilation when developing. In another tab/window, do this:

    $ lein cljsbuild auto

Whenever you save the ClojureScript file, it will be compiled. Since the JVM running
the compilation is already started, subsequent compilations will be fast:

    $ lein cljsbuild auto
    Compiling ClojureScript.
    Compiling "gameoflife.js" from ["src"]...
    Successfully compiled "gameoflife.js" in 7.656883 seconds.
    Compiling "gameoflife.js" from ["src"]...
    Successfully compiled "gameoflife.js" in 0.291744 seconds.
