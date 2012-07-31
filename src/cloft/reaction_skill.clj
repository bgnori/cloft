(ns cloft.reaction-skill
    (:require [cloft.cloft :as c])
    (:require [cloft.arrow-skill :as arrow-skill])
    (:require [cloft.skill :as skill])
    (:import [org.bukkit Material])
    (:import [org.bukkit.potion PotionEffect PotionEffectType])
    (:import [org.bukkit.inventory ItemStack])
    (:import [org.bukkit.entity IronGolem Player Projectile TNTPrimed Wolf])
    )

(def reaction-skill (atom {}))
(defn of [player]
  (when-let [[skill num-rest] (get @reaction-skill (.getDisplayName player))]
    (if (= 0 num-rest)
      (do
        (c/broadcast (format "%s lost reactio-skill %s" (.getDisplayName player) (c/skill2name skill)))
        (swap! reaction-skill assoc (.getDisplayName player) nil)
        nil)
      (do
        (swap! reaction-skill assoc (.getDisplayName player) [skill (dec num-rest)])
        skill))))

(defn of-without-consume [player]
  (first (get @reaction-skill (.getDisplayName player))))

(defn set-skill [lv player skill-name]
    (c/broadcast (.getDisplayName player) " changed reaction-skill to " (last skill-name))
    (.sendMessage player (format "You can use the reaction skill for %d times" lv))
    (swap! reaction-skill assoc (.getDisplayName player) [(first skill-name) lv]))

(defn redirect [skill attacker target]
  (let [actual-attacker
        (if (instance? Projectile attacker)
          (.getShooter attacker)
          attacker)]
    (when (and (not= actual-attacker target)
               (not (instance? Wolf actual-attacker))
               (not (instance? TNTPrimed actual-attacker))
               (not (and
                      (instance? Player actual-attacker)
                      (= arrow-skill/diamond (arrow-skill/of actual-attacker)))))
      (skill target actual-attacker))))

(defn ice [you by]
  (skill/freeze-for-20-sec by)
  (c/lingr (str "counter attack with ice by " (.getDisplayName you) " to " (c/entity2name by))))

(defn knockback [you by]
  (let [direction (.multiply (.normalize (.toVector (.subtract (.getLocation by) (.getLocation you)))) 2)]
    (c/add-velocity by (.getX direction) (.getY direction) (.getZ direction))))

(defn fire [you by]
  (.setFireTicks by 100))

(defn golem [you by]
  (let [golem (.spawn (.getWorld by) (.getLocation by) IronGolem)]
    (.setTarget golem by)
    (future-call #(do
                    (Thread/sleep 10000)
                    (.remove golem))))
  (.sendMessage you "a golem helps you!"))

(defn wolf [you by]
  (let [wolf (.spawn (.getWorld by) (.getLocation by) Wolf)]
    (.setTamed wolf true)
    (.setOwner wolf you)
    (.setTarget wolf by)
    (future-call #(do
                    (Thread/sleep 10000)
                    (.remove wolf))))
  (.sendMessage you "a wolf helps you!"))

(defn teleport [you by]
  (letfn [(find-place [from range]
            (let [candidates
                  (for [x range y range z range :when (> y 5)]
                    (.add (.clone (.getLocation from)) x y z))
                  good-candidates
                  (filter
                    #(and
                       (not= Material/AIR
                             (.getType (.getBlock (.add (.clone %) 0 -1 0))))
                       (= Material/AIR (.getType (.getBlock %)))
                       (= Material/AIR
                          (.getType (.getBlock (.add (.clone %) 0 1 0)))))
                    candidates)]
              (rand-nth good-candidates)))]
    (.sendMessage you
      (str "You got damage by " (c/entity2name by) " and escaped."))
    (.teleport you (find-place you (range -10 10)))))

(defn poison [you by]
  (.addPotionEffect by (PotionEffect. PotionEffectType/POISON 200 2)))

