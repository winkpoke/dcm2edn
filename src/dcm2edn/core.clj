(ns dcm2edn.core
  (:require [cheshire.core :as json]
            [clojure.pprint]
            [clojure.java.io :as io])
  (:import (org.dcm4che3.tool.dcm2json Dcm2Json)
           (org.dcm4che3.io DicomInputStream)
           (java.io File ByteArrayOutputStream PrintStream)
           ;(javafx.scene.image WritableImage)
           (java.nio ByteBuffer ByteOrder)
           )
  (:gen-class
    :main true))

(def dcmfile "/Users/philchen/Project/database_test/eclipse/imrt/CT.RT001921_1.dcm")
(def dcmfile2 "C:\\dicom.dcm")

(def ^:dynamic *dcm-encoding* "UTF8")

(defn read-file
  ([file]
  (with-open [byte-array (ByteArrayOutputStream.)
              output-new (PrintStream. byte-array)]
    (let [output-old (System/out)]
      (System/setOut output-new)
      (->> (File. file)
         (DicomInputStream.)
         (.parse (Dcm2Json.)))
      (System/setOut output-old)
      (-> (.toString byte-array *dcm-encoding*)
          (json/parse-string)))))
  ([file encoding]
  (binding [*dcm-encoding* encoding]
    (read-file file))))

(def buf (ByteBuffer/allocate 600000))
(def f (io/input-stream dcmfile))
(.order buf ByteOrder/LITTLE_ENDIAN)
(.read f (.array buf) 1728 524288)
(def coll (repeatedly #(.getShort buf)))

;(def img (WritableImage. 512 512))
;(def buf (byte-array 600000))
;(def f (input-stream dcmfile))
;(.read f 1728 524288)

(defn -main
  [file]
  (println file)
  (clojure.pprint/pprint (read-file file))
)
;(import 'java.nio.ByteBuffer)