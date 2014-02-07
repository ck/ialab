(defproject ialab "0.1.0-SNAPSHOT"
  :description "Immutant Aleph Lab"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.slf4j/slf4j-log4j12 "1.6.4"]
                 [clj-time "0.6.0"]
                 [aleph "0.3.0"]]
  :javac-options ["-target" "1.7" "-source" "1.7"]
  :source-paths      ["src/clj"]
  :java-source-paths ["src/java"]
  :test-paths        ["test/clj"]
  :immutant {:context-path "/ialab"}
  :profiles {:dev        {:source-paths ^:displace ["dev"]
                          :dependencies [[org.clojure/tools.namespace "0.2.3"]
                                         [org.clojure/java.classpath "0.2.0"]]
                          :immutant {:init        immutant.system/init
                                     :environment "development"}}
             :production {:immutant {:init        immutant.system/init
                                     :environment "production"}}}
  :repositories {"sonatype-oss-public"
                 "https://oss.sonatype.org/content/groups/public/"}
  :warn-on-reflection false
  :min-lein-version "2.0.0")
