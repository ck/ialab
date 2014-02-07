(ns ialab.math-test
  (:require [clojure.test :refer :all]
            [ialab.math :refer :all]))


(deftest test-gcd

  (is (= 0 (gcd 0 0))))
