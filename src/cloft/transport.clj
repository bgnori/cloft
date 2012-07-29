(ns cloft.transport
    (:require [cloft.ujm :as ujm])
    (:require [cloft.cloft :as c])
    (:require [cloft.player :as player])
    (:import [org.bukkit.util Vector])
    (:import [org.bukkit Bukkit Material])
    (:import [org.bukkit Location Effect])
    (:import [org.bukkit.entity Player])
    )


(def world (Bukkit/getWorld "world"))

(defn player-teleport-machine [evt player]
  (when (and
          (= (.getWorld player) world)
          (< (.distance ujm/place2 (.getLocation player)) 1))
    (c/lingr (str (.getDisplayName player) " is teleporting..."))
    (.setTo evt ujm/place3))
  (when (and
          (= (.getWorld player) world)
          (< (.distance ujm/place1 (.getLocation player)) 1)
          (.isLoaded (.getChunk ujm/place4)))
    (c/lingr (str (.getDisplayName player) " is teleporting..."))
    (.setTo evt ujm/place4))
  (when (and
          (= (.getWorld player) world)
          (< (.distance ujm/place5 (.getLocation player)) 1)
          (.isLoaded (.getChunk ujm/place6)))
    (c/lingr (str (.getDisplayName player) " is teleporting..."))
    (.setTo evt ujm/place6))
  (when (and
          (= (.getWorld player) world)
          (or
            (< (.distance ujm/place9 (.getLocation player)) 1)
            (< (.distance ujm/place10 (.getLocation player)) 1))
          (.isLoaded (.getChunk ujm/place-main)))
    (c/lingr (str (.getDisplayName player) " is teleporting..."))
    (.setTo evt ujm/place-main))
  (when (and
          (= (.getWorld player) world)
          (< (.distance ujm/place7 (.getLocation player)) 1))
    (let [death-point (player/death-location-of player)]
      (when death-point
        (.isLoaded (.getChunk death-point)) ; for side-effect
        (c/lingr (str (.getDisplayName player) " is teleporting to the last death place..."))
        (.setTo evt death-point)))))



(defn teleport-machine? [loc]
  (=
    (for [x [-1 0 1] z [-1 0 1]]
      (if (and (= x 0) (= z 0))
        'any
        (.getType (.getBlock (.add (.clone loc) x 0 z)))))
    (list Material/GLOWSTONE Material/GLOWSTONE Material/GLOWSTONE
          Material/GLOWSTONE 'any Material/GLOWSTONE
          Material/GLOWSTONE Material/GLOWSTONE Material/GLOWSTONE)))


(defn teleport-up [entity block]
  (when (#{Material/STONE_PLATE Material/WOOD_PLATE} (.getType block))
    (let [entity-loc (.getLocation entity)
          loc (.add (.getLocation block) 0 -1 0)]
      (when (teleport-machine? loc)
        (when (instance? Player entity)
          (.sendMessage entity "teleport up!"))
        (future-call #(let [newloc (.add (.getLocation entity) 0 30 0)]
                        (Thread/sleep 10)
                        (condp = (.getType block)
                          Material/STONE_PLATE (.teleport entity newloc)
                          Material/WOOD_PLATE (c/add-velocity entity 0 1.5 0)
                          nil)
                        (.playEffect (.getWorld entity-loc) (.add entity-loc 0 1 0) Effect/BOW_FIRE nil)
                        (.playEffect (.getWorld newloc) newloc Effect/BOW_FIRE nil)
                        (.playEffect (.getWorld entity-loc) entity-loc Effect/ENDER_SIGNAL nil)
                        (.playEffect (.getWorld newloc) newloc Effect/ENDER_SIGNAL nil)))))))


(defn teleport-machine [player block block-against]
  (when (= Material/WATER (.getType block))
    (let [wools (for [x [-1 0 1] z [-1 0 1] :when (or (not= x 0) (not= z 0))]
                  (.getType (.getBlock
                              (.add (.clone (.getLocation block-against)) x 0 z))))]
      nil)))
