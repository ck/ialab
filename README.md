# Immutant Aleph Lab #

Experiments with [Immutant][immutant] and [Aleph][aleph].

## Development ##

Deploy to local Immutant instance

```
lein immutant deploy
```

Start Immutant

```
lein immutant run
```

This starts the system automagically.

A TCP server listens on port 4444 for strings.
To send a message execute

```
echo hello | nc localhost 4444
```


## Build ##

The (recommended) deployment artifact is an Immutant archive,
which can be build by running

```
lein with-profile production immutant archive
```

The resulting file, `ialab.ima`, can then be deployed.


## License ##

    Copyright © 2013 Christian Kebekus

    The use and distribution terms for this software are covered by the
    Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
    which can be found in the file [epl-v10.html](epl-v10.html) at the
    root of this distribution.

    By using this software in any fashion, you are agreeing to be bound by
    the terms of this license.

    You must not remove this notice, or any other, from this software.


[aleph]: https://github.com/ztellman/aleph
[immutant]: http://immutant.org
