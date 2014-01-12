# Conway's Game of Life

This is a ClojureScript implementation of Conway's Game of Life.

## Running it

1. Make sure you have [Leiningen](http://leiningen.org/) 2.x installed.

2. Compile the ClojureScript code to Javascript:

        $ lein cljsbuild once dev

3. Open `index.html` in your browser.

## Developing

It's convenient with auto-compilation when developing. In another tab/window, do this:

    $ lein cljsbuild auto dev

Whenever you save the ClojureScript file, it will be compiled. Since the JVM running
the compilation is already started, subsequent compilations will be fast:

    $ lein cljsbuild auto
    Compiling ClojureScript.
    Compiling "gameoflife.js" from ["src"]...
    Successfully compiled "gameoflife.js" in 7.656883 seconds.
    Compiling "gameoflife.js" from ["src"]...
    Successfully compiled "gameoflife.js" in 0.291744 seconds.

## Production

There are two build profiles: `dev` and `prod`. The `prod` profile uses advanced
optimization. It takes into account all the source files and any libraries used,
and performs not only aggressive minification, but it also uses highly sophisticated
techniques for identifying and eliminating dead code (i.e. code that is never called
nor reachable). Using the `prod` profile squeezes down this project to a single file
of around 20K zipped Javascript, all included.

The `prod` profile is used by:

1. Compile with the `prod` profile (or none, which will compile all profiles):

        $ lein cljsbuild once prod
        $ lein cljsbuild once

2. Comment out the script lines in `index.html` used for the `dev` profile:

        <!-- for production code, comment out these lines -->
        <script src="out/goog/base.js" type="text/javascript"></script>
        <script src="gameoflife-debug.js" type="text/javascript"></script>
        <script type="text/javascript">goog.require("gameoflife.core");</script>

3. Uncomment the single script line in `index.html` needed for production:

        <!-- this is the only script line needed for production
        <script src="gameoflife.js" type="text/javascript"></script>
        -->

The only deliverables when using the `prod` profile are:

* `index.html`
* `main.css`
* `gameoflife.js`
