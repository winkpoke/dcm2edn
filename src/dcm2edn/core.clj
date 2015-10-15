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
;(set! *warn-on-reflection* true)
;(def dcmfile "/Users/philchen/Project/database_test/eclipse/imrt/CT.RT001921_1.dcm")
(def dcmfile "/Users/philchen/Project/database_test/Pinnacle/cao wen juan_CRT/CT.1.2.840.113619.2.25.4.2147483647.1439195343.543.dcm")
(def dcmfile2 "C:\\dicom.dcm")

(def dcm-img-offset 2404)
(def dcm-img-size (* 512 512 2))
(def byte-order :little-endian)

(def window-level 40.0)
(def window-width 400.0)

(def ^:dynamic *dcm-encoding* "UTF8")

(defn dcm-input-stream
  [^String fn]
  (-> (File. fn)
      (DicomInputStream.)))

(defn read-file
  ([file]
  (with-open [byte-array (ByteArrayOutputStream.)
              output-new (PrintStream. byte-array)]
    (let [output-old (System/out)]
      (System/setOut output-new)
      (->> (File. ^String file)
         (DicomInputStream.)
         (.parse (Dcm2Json.)))
      (System/setOut output-old)
      (-> (.toString byte-array ^String *dcm-encoding*)
          (json/parse-string)))))
  ([file encoding]
  (binding [*dcm-encoding* encoding]
    (read-file file))))

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
        :else      (Math/round
                     (* 255
                        (/ (- v k-)
                           ww)))))))
;(time (do

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

(defn calc-image-byte-buff
  [f]
  (delay
    (let [^ByteBuffer img-buf (ByteBuffer/allocate (* 3 (/ dcm-img-size 2)))
          ^bytes img-array (.array ^ByteBuffer img-buf)
          laps (/ dcm-img-size 2)]
      (loop [i 0]
        (when (< i laps)
          (let [v (aget ^shorts pixel-value-array i)
                pixel-value (byte (- (short (f v)) 128))
                idx (* i 3)]
            (aset ^bytes img-array idx pixel-value)
            (aset ^bytes img-array (+ 1 idx) pixel-value)
            (aset ^bytes img-array (+ 2 idx) pixel-value)
            (recur (inc i)))))
      img-array)))

(defn get-image-byte-buff []
  (calc-image-byte-buff (winlevel window-level window-width)))

;(def coll (repeatedly #(.getShort buf)))
;(.close f)

(deref (get-image-byte-buff))
;))



(defn -main
  [file]
  (println file)
  (clojure.pprint/pprint (read-file file))
)

; -----------------------------------------------------------
; REPL


(def img (deref (get-image-byte-buff)))
(for [i (take 1500000 img) :when (> i -50)] i)


(+ 1 1)


(take 1000 pixel-value-array)

;(pprint (read-file dcmfile))

;(def buf (ByteBuffer/allocate dcm-img-size))
;(def f (io/input-stream dcmfile))
;(.order buf (case byte-order
;              :little-endian ByteOrder/LITTLE_ENDIAN
;              :big-endian ByteOrder/BIG_ENDIAN
;              ByteOrder/LITTLE_ENDIAN))
;(.skip f dcm-img-offset)
;(.read f (.array buf) 0 dcm-img-size)
;
;(def pixel-value-array (short-array (/ dcm-img-size 2)))
;(.rewind buf)
;(-> buf
;    (.asShortBuffer)
;    (.get pixel-value-array))

;(import 'java.nio.ByteBuffer)

;(def func (hu2gray window-level window-width))

;(def img-buf (ByteBuffer/allocate 786432))
;(def ^bytes img-array (.array ^ByteBuffer img-buf))

;(def ff (map #(* 2.0 %) coll))

;(def img (WritableImage. 512 512))
;(def buf (byte-array 600000))
;(def f (input-stream dcmfile))
;(.read f 1728 524288)
