(ns dcm2edn.core
  (:require [cheshire.core :as json]
            [clojure.pprint]
            [clojure.java.io :as io])
  (:import [org.dcm4che3.tool.dcm2json Dcm2Json]
           [org.dcm4che3.io DicomInputStream DicomInputStream$IncludeBulkData]
           [java.io File ByteArrayOutputStream PrintStream OutputStream]
           [java.nio ByteBuffer ByteOrder]
           [javax.json Json]
           [org.dcm4che3.json JSONWriter])
  (:gen-class
   :main true))


;(set! *warn-on-reflection* true)
;(def dcmfile "/Users/philchen/Project/database_test/eclipse/imrt/CT.RT001921_1.dcm")


(def tag-window-center "00281050")
(def tag-window-width "00281051")

(def dcmfile "/Users/philchen/Project/database_test/Pinnacle/cao wen juan_CRT/CT.1.2.840.113619.2.25.4.2147483647.1439195343.543.dcm")
(def dcmfile2 "C:\\dicom.dcm")

(def dcmdir "/Users/philchen/Project/database_test/Pinnacle/cao wen juan_CRT/")

(def dcm-img-offset 2404)
(def dcm-img-size (* 512 512 2))
(def byte-order :little-endian)

(def window-level 40.0)
(def window-width 400.0)

(def ^:dynamic *dcm-encoding* "UTF8")

(defn vget
  [edn tag]
  (get-in edn [tag "Value"]))

(defn create-json-generator [^OutputStream out]
  (let [m (hash-map)]
    (.createGenerator (Json/createGeneratorFactory m) out)))

(defn parse
  [^DicomInputStream in out]
  (doto in
    (.setBulkDataDirectory nil)
    (.setBulkDataFilePrefix "blk")
    (.setBulkDataFileSuffix nil)
    (.setConcatenateBulkDataFiles false)
    (.setIncludeBulkData DicomInputStream$IncludeBulkData/URI))
  (let [json-gen (create-json-generator out)
        json-writer (JSONWriter. json-gen)]
    (doto in
      (.setDicomInputHandler json-writer)
      (.readDataset -1 -1))
    (.flush json-gen)))

(defn- read-file-impl
  [f & {:keys [encoding]
        :or {encoding "UTF8"}}]
  (with-open [byte-array (ByteArrayOutputStream.)
              output-new (PrintStream. byte-array)]
    (-> f
        (DicomInputStream.)
        (parse output-new))
    (-> (.toString byte-array ^String encoding)
        (json/parse-string))))

(defmulti read-file
  (fn [f & _]
    (cond
      (instance? String f) :string
      (instance? File f) :file
      :else :default)))

(defmethod read-file :string
  [f & r]
  (apply read-file-impl (cons (File. f) r)))

(defmethod read-file :file
  [f & r]
  (apply read-file-impl (cons f r)))

(defn winlevel
  "Return a function that calculates the gray value [0, 255] 
   in terms of window level and window width."
  [wl  ww]
  (let [half-ww (/ ww 2.0)
        k-      (- wl half-ww)
        k+      (+ wl half-ww)]
    (fn [v]
      (cond
        (<= v k-) 0
        (>= v k+) 255
        :else     (Math/round
                    (* 255
                       (/ (- v k-)
                          ww)))))))

(defn read-image-data
  [f offset len & {:keys [byte-order]
                   :or {byte-order :little-endian}} ]
  (let [buf (ByteBuffer/allocate len)] 
    (.order buf (case byte-order
                  :little-endian ByteOrder/LITTLE_ENDIAN
                  :big-endian ByteOrder/BIG_ENDIAN
                  ByteOrder/LITTLE_ENDIAN))
    (doto f
      (.skip offset)
      (.read (.array buf) 0 len))
    (let [pixel-value-array (short-array (/ len 2))]
      (.. buf
          (rewind)
          (asShortBuffer)
          (get pixel-value-array))
      pixel-value-array)))

(def pixel-value-array
  (read-image-data
    (io/input-stream dcmfile)
    dcm-img-offset dcm-img-size
    :byte-order :little-endian))

(defmacro gbyte
  "Convert a java short within the range [0, 255] to a java byte.
   The input value must be between [0, 255], otherwise it throws
   IllegalArgumentException. Use macro for performace concern."
  [f v]
  `(byte (- (short (~f ~v)) 128)))


(time (do

(defn calc-image-byte-buff
  [f ^shorts pixel-array]
  (delay
   (let [size (alength pixel-array)
         ^bytes img-array (byte-array (* 3 size))
         laps size]
     (loop [i 0]
       (when (< i laps)
         (let [v (aget ^shorts pixel-array i)
               pixel-value (gbyte f v)
               idx (* i 3)]
           ;(print i " ")
           (aset ^bytes img-array idx pixel-value)
           (aset ^bytes img-array (+ 1 idx) pixel-value)
           (aset ^bytes img-array (+ 2 idx) pixel-value)
           (recur (inc i)))))
     img-array)))

(defn get-image-byte-buff []
  (calc-image-byte-buff (winlevel window-level window-width)
                        pixel-value-array))

;; (defn get-image-byte-buff []
;;   (calc-image-byte-buff (winlevel-inline window-level window-width)
;;                         pixel-value-array))

;(def coll (repeatedly #(.getShort buf)))
;(.close f)

(deref (get-image-byte-buff))
))


(defn -main
  [file]
  (println file)
  (clojure.pprint/pprint (read-file file))
)

