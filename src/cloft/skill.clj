(ns cloft.skill
    (:require [cloft.cloft :as c])
    (:import [org.bukkit Material])
    )



(defn freeze-for-20-sec [target]
  (when-not (.isDead target)
    (let [loc (.getLocation (.getBlock (.getLocation target)))]
      (doseq [y [0 1]]
        (doseq [[x z] [[-1 0] [1 0] [0 -1] [0 1]]]
          (let [block (.getBlock (.add (.clone loc) x y z))]
            (when (#{Material/AIR Material/SNOW} (.getType block))
              (.setType block Material/GLASS)))))
      (doseq [y [-1 2]]
        (let [block (.getBlock (.add (.clone loc) 0 y 0))]
          (when (#{Material/AIR Material/SNOW} (.getType block))
            (.setType block Material/GLASS))))
      (future-call #(do
                      (Thread/sleep 1000)
                      (when-not (.isDead target)
                        (.teleport target (.add (.clone loc) 0.5 0.0 0.5)))))
      (future-call #(do
                      (Thread/sleep 20000)
                      (doseq [y [0 1]]
                        (doseq [[x z] [[-1 0] [1 0] [0 -1] [0 1]]]
                          (let [block (.getBlock (.add (.clone loc) x y z))]
                            (when (= (.getType block) Material/GLASS)
                              (.setType block Material/AIR)))))
                      (doseq [y [-1 2]]
                        (let [block (.getBlock (.add (.clone loc) 0 y 0))]
                          (when (= (.getType block) Material/GLASS)
                            (.setType block Material/AIR)))))))))
