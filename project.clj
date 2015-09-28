(defproject dcm2edn "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [cheshire "5.5.0"]
                 [seesaw "1.4.5"]
                 ;[org.dcm4che3.tool.dcm2json "3.3.7"]
                 [org.dcm4che/dcm4che-core "3.3.7"]
                 [org.dcm4che.tool/dcm4che-tool-dcm2json "3.3.7"]
                ]
  :repositories [["dcm4che" "http://www.dcm4che.org/maven2"]]
  :main dcm2edn.core
  :aot :all)
