(ns dcm2edn.core
  (:require [cheshire.core :as json]
            [clojure.pprint])
  (:import (org.dcm4che3.tool.dcm2json Dcm2Json)
           (org.dcm4che3.io DicomInputStream)
           (java.io File ByteArrayOutputStream PrintStream))
  (:gen-class
    :main true))

(def dcmfile "/Users/philchen/Project/database_test/eclipse/imrt/CT.RT001921_1.dcm")

(defn read-file
  [file]
  (with-open [byte-array (ByteArrayOutputStream.)
              output-new (PrintStream. byte-array)]
    (let [output-old (System/out)]
      (System/setOut output-new)
      (->> (File. file)
         (DicomInputStream.)
         (.parse (Dcm2Json.)))
      (System/setOut output-old)
      (-> (.toString byte-array "UTF8")
          (json/parse-string)))))


(defn -main
  [file]
  (println file)
  (clojure.pprint/pprint (read-file file))
)
