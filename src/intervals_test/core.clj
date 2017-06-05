(ns intervals-test.core
  (:require [clojure.set :refer [difference]]
            [clojure.pprint :refer [pprint]])
  (:gen-class)
  (:import (com.brein.time.timeintervals.collections ListIntervalCollection IntervalCollectionFactory)
           (com.brein.time.timeintervals.indexes IntervalTreeBuilder$IntervalType IntervalTreeBuilder IntervalTree)
           (com.brein.time.timeintervals.intervals IdInterval LongInterval)))

(defn -main
  [& args]
  (println "Starting...")
  (let [sample  (read-string (slurp "long-interval.sample"))

        factory (reify IntervalCollectionFactory
                  (load [_ k]
                    (ListIntervalCollection.)))

        tree    ^IntervalTree
                (-> (IntervalTreeBuilder/newBuilder)
                    (.usePredefinedType IntervalTreeBuilder$IntervalType/NUMBER)
                    (.collectIntervals factory)
                    .build)]

    ; bang on tree in place, not very functional :)
    (reduce (fn [t [id start end]]
              (let [long-int (LongInterval.
                               start (if (= end Long/MAX_VALUE) nil end)
                               false true)]
                (doto t
                  (.add (IdInterval. id long-int)))))
            tree sample)

    (let [now        (System/currentTimeMillis)
          query      (LongInterval. now now)
          stab-query (.overlap tree query)
          seq-query  (filter #(.overlaps query %) tree)
          stab-set   (into #{} stab-query)
          seq-set    (into #{} seq-query)]

      (println "Stab query count" (count stab-query) "unique" (count stab-set))
      (println "Seq query count" (count seq-query) "unique" (count seq-set))

      (println "Only in stab query:")
      (pprint (difference stab-set seq-set))
      (println "Only in seq query")
      (pprint (difference seq-set stab-set))))

  (println "Done."))
