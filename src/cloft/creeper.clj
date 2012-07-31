(ns cloft.creeper
    (:require [cloft.cloft :as c])
    (:require [cloft.sanctuary :as sanctuary])
    (:import [org.bukkit Bukkit Material])
    (:import [org.bukkit.entity Creeper LivingEntity Player TNTPrimed Wolf])
    (:import [org.bukkit.util Vector])
    )


(defn explosion-1 [evt entity]
  (.setCancelled evt true)
  (.createExplosion (.getWorld entity) (.getLocation entity) 0)
  (doseq [e (filter #(instance? LivingEntity %) (.getNearbyEntities entity 5 5 5))]
    (let [v (.multiply (.toVector (.subtract (.getLocation e) (.getLocation entity))) 2.0)
          x (- 5 (.getX v))
          z (- 5 (.getZ v))]
      (when (instance? Player e)
        (.sendMessage e "Air Explosion"))
      (.setVelocity e (Vector. x 1.5 z))))
  (comment (let [another (.spawn (.getWorld entity) (.getLocation entity) Creeper)]
             (.setVelocity another (Vector. 0 1 0)))))

(defn explosion-2 [evt entity]
  (.setCancelled evt true)
  (if (sanctuary/is-in? (.getLocation entity))
    (prn 'cancelled)
    (let [loc (.getLocation entity)]
      (.setType (.getBlock loc) Material/PUMPKIN)
      (c/broadcast "break the bomb before it explodes!")
      (future-call #(do
                      (Thread/sleep 7000)
                      (c/broadcast "zawa...")
                      (Thread/sleep 1000)
                      (when (= (.getType (.getBlock loc)) Material/PUMPKIN)
                        (.setType (.getBlock loc) Material/AIR)
                        (let [tnt (.spawn (.getWorld loc) loc TNTPrimed)]
                          (Thread/sleep 1000)
                          (.remove tnt)
                          (.createExplosion (.getWorld loc) loc 6 true))))))))

(defn explosion-3 [evt entity]
  (.setCancelled evt true)
  (.createExplosion (.getWorld entity) (.getLocation entity) 0))


(def explosion-idx (atom 0))

(defn explosion-next-idx []
  (swap! explosion-idx inc))

(defn current-explosion []
  (get [(fn [_ _] nil)
        explosion-1
        explosion-2
        explosion-3
        ] (rem @explosion-idx 4)))

;; wtf is this?
(defn entity-explosion [evt]
     (prn 'entity-explosion (.getEntity evt))
     (if (= (rem @explosion-idx 3) 0)
       (.setDamage evt (min (.getDamage evt) 19))
       (.setDamage evt 0)))

(def good-bye (partial c/good-bye Creeper (Bukkit/getWorld "world")))

