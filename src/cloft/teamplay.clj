(ns cloft.teamplay
    (:require [cloft.cloft :as c])
    (:import [org.bukkit.entity Monster ])
    )


(def last-vertical-shots (atom {}))


(defn update-last-vertical-shot [shooter]
  (swap! last-vertical-shots assoc (.getDisplayName shooter) (.getLocation shooter)))


(defn enough-previous-shots-by-players? [triggered-by threshold]
  (let [locs (vals @last-vertical-shots)]
    ;(prn enough-previous-shots-by-players?)
    ;(prn locs)
    (<= threshold
      (count (filter
               #(> 10.0 ; radius of 10.0
                   (.distance (.getLocation triggered-by) %))
               locs)))))


(defn thunder-mobs-around [player amount]
  (doseq [x (filter
              #(instance? Monster %)
              (.getNearbyEntities player 20 20 20))]
    (Thread/sleep (rand-int 1000))
    (.strikeLightningEffect (.getWorld x) (.getLocation x))
    (.damage x amount)))


(defn check-and-thunder [triggered-by]
  (when (enough-previous-shots-by-players? triggered-by 3)
    (future-call #(thunder-mobs-around triggered-by 20))))
    ; possiblly we need to flush last-vertical-shots, not clear.
    ; i.e. 3 shooters p1, p2, p3 shoot arrows into mid air consecutively, how often thuders(tn)?
    ; A.
    ; p1, p2, p3, p1, p2, p3,
    ;         t1          t2
    ; B.
    ; p1, p2, p3, p1, p2, p3,
    ;         t1, t2, t3, t4
;

(defn wait-for-other-vertical-shot [shooter]
  (future-call #(let [shooter-name (.getDisplayName shooter)]
                  (check-and-thunder shooter)
                  (Thread/sleep 1000)
                  (swap! last-vertical-shots dissoc shooter-name))))


