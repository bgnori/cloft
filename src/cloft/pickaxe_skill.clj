(ns cloft.pickaxe-skill
    (:require [cloft.cloft :as c])
    (:import [org.bukkit Effect Material])
    )

(def pickaxe-skill (atom {}))
(defn of [player]
  (get @pickaxe-skill (.getDisplayName player)))


(defn set-skill [player skill-name]
  (c/broadcast (.getDisplayName player) " changed pickaxe-skill to " (last skill-name))
  (swap! pickaxe-skill assoc (.getDisplayName player) (first skill-name)))

(defn stone [player evt]
  (if (= Material/STONE (.getType (.getBlock evt)))
    (.setInstaBreak evt true)
    (when (not= 0 (rand-int 1000))
      (.setCancelled evt true))))

(defn teleport [player item]
  (future-call
    #(do
       (Thread/sleep 2000)
       (when-not (.isDead item)
                 (c/teleport-without-angle player (.getLocation item))))))

(defn fire [player target]
  (.setFireTicks target 200))

(defn ore [block evt]
  (letfn [(f [blocktype]
             (.setType block blocktype)
             (.setCancelled evt true)
             (.playEffect (.getWorld block) (.getLocation block) Effect/MOBSPAWNER_FLAMES nil))]
         (cond
           (= 0 (rand-int 10)) (f Material/COAL_ORE)
           (= 0 (rand-int 20)) (f Material/IRON_ORE)
           (= 0 (rand-int 30)) (f Material/REDSTONE_ORE)
           (= 0 (rand-int 40)) (f Material/LAPIS_ORE)
           (= 0 (rand-int 50)) (f Material/GOLD_ORE)
           (= 0 (rand-int 1000)) (f Material/DIAMOND_ORE)
           (= 0 (rand-int 300)) (f Material/GLOWSTONE)
           (= 0 (rand-int 1000)) (f Material/LAPIS_BLOCK)
           (= 0 (rand-int 1500)) (f Material/GOLD_BLOCK)
           (= 0 (rand-int 2000)) (f Material/GOLD_BLOCK)
           (= 0 (rand-int 50000)) (f Material/DIAMOND_BLOCK)
           :else nil)))


