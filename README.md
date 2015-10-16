# dcm2edn

A Clojure library designed to read a DICOM file and transform the contained information into Clojure EDN format.

It's still under development and not yet done the functionality.

## Usage

For now, you need to git clone the repository and do the following:

```clojure
lein install
```

```clojure
(ns example.core
  (:require [dcm2edn.core :as dicom]))

(let [dcm (dicom/read-file "path/to/your/dicom/file")]
  (println dcm))

```

## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
