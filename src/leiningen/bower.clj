(ns leiningen.bower
  (:require [leiningen.help :as help]
            [leiningen.core.main :as main]
            [leiningen.npm :refer
             [with-json-file environmental-consistency transform-deps]]
            [cheshire.core :as json]
            [leiningen.npm.deps :refer [resolve-node-deps]]
            [leiningen.npm.process :refer [exec]]
            [robert.hooke]
            [leiningen.deps]))

(defn project->bowerrc
  [project]
  (json/generate-string
   {:directory (project :bower-directory)}))

(defn project->component
  [project]
  (json/generate-string
   {:name (project :name)
    :description (project :description)
    :version (project :version)
    :dependencies (transform-deps
                   (resolve-node-deps :bower-dependencies project))}))

(defn- invoke
  [project & args]
  (exec (project :root) (cons "bower" args)))

(defn bower
  "Invoke the Bower component manager."
  ([project]
     (environmental-consistency project "component.json" ".bowerrc")
     (println (help/help-for "bower"))
     (main/abort))
  ([project & args]
     (environmental-consistency project "component.json" ".bowerrc")
     (with-json-file
    "component.json" (project->component project) project
    (with-json-file
      ".bowerrc" (project->bowerrc project) project
      (apply invoke project args)))))

(defn install-deps
  [project]
  (environmental-consistency project)
  (with-json-file
    "component.json" (project->component project) project
    (with-json-file
      ".bowerrc" (project->bowerrc project) project
      (invoke project "run-script" "bower"))))

(defn wrap-deps
  [f & args]
  (apply f args)
  (install-deps (first args)))

(defn install-hooks []
  (robert.hooke/add-hook #'leiningen.deps/deps wrap-deps))
