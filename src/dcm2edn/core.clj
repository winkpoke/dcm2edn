(ns dcm2edn.core
  (:require [cheshire.core :as json]
            [clojure.pprint])
  (:import (org.dcm4che3.tool.dcm2json Dcm2Json)
           (org.dcm4che3.io DicomInputStream)
           (java.io File ByteArrayOutputStream PrintStream))
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

(defn -main
  [file]
  (println file)
  (clojure.pprint/pprint (read-file file))
)
