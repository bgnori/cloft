(ns cloft.arrow-skill
    (:require [cloft.cloft :as c])
    (:import [org.bukkit Effect Material])
    (:import [org.bukkit.inventory ItemStack])
    (:import [org.bukkit.entity Arrow Item LivingEntity Sheep])
    )

(def arrow-skill (atom {}))
(defn of [player]
  (get @arrow-skill (.getDisplayName player)))

(defn set-skill [player skill-name]
     (c/broadcast (.getDisplayName player) " changed arrow-skill to " (last skill-name))
     (swap! arrow-skill assoc (.getDisplayName player) (first skill-name)))


(defn block-of-arrow [entity]
  (let [location (.getLocation entity)
        velocity (.getVelocity entity)
        direction (.multiply (.clone velocity) (double (/ 1 (.length velocity))))]
    (.getBlock (.add (.clone location) direction))))


(defn explosion [entity]
  (.createExplosion (.getWorld entity) (.getLocation entity) 0)
  (let [block (block-of-arrow entity)]
    (.breakNaturally block (ItemStack. Material/DIAMOND_PICKAXE)))
  (.remove entity))

(defn torch [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)]
    (.setType (.getBlockAt world location) Material/TORCH)))

(defn pull [entity]
  (let [block (block-of-arrow entity)]
    (if (c/removable-block? block)
      (let [shooter-loc (.getLocation (.getShooter entity))]
        (.setType (.getBlock shooter-loc) (.getType block))
        (when (= Material/MOB_SPAWNER (.getType block))
          (.setSpawnedType (.getState (.getBlock shooter-loc)) (.getSpawnedType (.getState block))))
        (.setType block Material/AIR)
        (.teleport (.getShooter entity) (.add shooter-loc 0 1 0)))
      (.sendMessage (.getShooter entity) "PULL failed")))
  (.remove entity))

(defn teleport [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)
        shooter (.getShooter entity)]
    (.setFallDistance shooter 0.0)
    (c/teleport-without-angle shooter location)))

(defn fire [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)]
    (doseq [target (filter
                     #(and (instance? LivingEntity %) (not= (.getShooter entity) %))
                     (.getNearbyEntities entity 1 1 1))]
      (.setFireTicks target 200))))

(defn flame [entity]
  (doseq [x [-1 0 1] y [-1 0 1] z [-1 0 1]
          :let [block (.getBlock (.add (.clone (.getLocation entity)) x y z))]
          :when (= Material/AIR (.getType block))]
    (.setType block Material/FIRE)))

(defn tree [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)]
    (.generateTree world location org.bukkit.TreeType/BIRCH)))

(defn web [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)]
    (.setType (.getBlockAt world location) Material/WEB)))

(defn ore [entity]
  (let [block (block-of-arrow entity)]
    (when (= (.getType block) Material/STONE)
      (let [block-to-choices [Material/COAL_ORE
                              Material/COAL_ORE
                              Material/COBBLESTONE
                              Material/COBBLESTONE
                              Material/GRAVEL
                              Material/IRON_ORE
                              Material/LAPIS_ORE
                              Material/GOLD_ORE
                              Material/REDSTONE_ORE]]
        (.setType block (rand-nth block-to-choices))))))

(defn shotgun [entity]
  (.remove entity))

(defn ice [entity]
  (if (.isLiquid (.getBlock (.getLocation entity)))
    (.setType (.getBlock (.getLocation entity)) Material/ICE)
    (let [block (block-of-arrow entity)
          loc (.getLocation block)
          loc-above (.add (.clone loc) 0 1 0)]
      (when
        (and
          (= Material/AIR (.getType (.getBlock loc-above)))
          (not= Material/AIR (.getType (.getBlock loc))))
        (.setType (.getBlock loc-above) Material/SNOW))
      (.dropItem (.getWorld loc-above) loc-above (ItemStack. Material/ARROW))))
  (.remove entity))

(defn pumpkin [entity]
  (future-call
    #(do
       (Thread/sleep 10)
       (when-not (.isDead entity)
         (let [block (.getBlock (.getLocation entity))]
           (if (and
                   (= 0 (rand-int 3))
                   (= Material/AIR (.getType block)))
             (do
               (.setType block (rand-nth [Material/PUMPKIN Material/JACK_O_LANTERN]))
               (.remove entity))
             (.sendMessage (.getShooter entity) "PUMPKIN failed")))))))

(defn plant [entity]
  (let [inventory (.getInventory (.getShooter entity))]
    (.remove entity)
    (doseq [x (range -3 4) z (range -3 4)]
      (let [loc (.add (.getLocation entity) x 0 z)]
        (when (and (.contains inventory Material/SEEDS)
                   (= Material/AIR (.getType (.getBlock loc)))
                   (= Material/SOIL (.getType (.getBlock (.add (.clone loc) 0 -1 0)))))
          (c/consume-itemstack inventory Material/SEEDS)
          (c/consume-itemstack inventory Material/SEEDS)
          (.setType (.getBlock loc) Material/CROPS))))))

(defn diamond [entity]
  (let [block (.getBlock (.getLocation entity))]
    (condp = (.getType block)
      Material/CROPS
      (.setData block 7)
      nil))
  (let [block (block-of-arrow entity)]
    (condp = (.getType block)
      Material/COBBLESTONE
      (.setType block Material/STONE)
      Material/WOOL
      (do
        (.setType block Material/AIR)
        (if (= 0 (rand-int 2))
          (.dropItem (.getWorld entity) (.getLocation entity) (ItemStack. Material/WOOL))
          (.spawn (.getWorld entity) (.getLocation entity) Sheep)))
      nil))
  (.remove entity)
  (let [loc (.getLocation entity)]
    (.dropItem (.getWorld loc) loc (ItemStack. Material/ARROW))))

(defn something-like-quake [entity klass f]
  (let [targets (.getNearbyEntities entity 5 3 5)]
    (future-call
      #(do
         (doseq [_ [1 2 3]]
           (doseq [target targets :when (instance? klass target)]
             (Thread/sleep (rand-int 300))
             (.playEffect (.getWorld target) (.getLocation target) Effect/ZOMBIE_CHEW_WOODEN_DOOR nil)
             (c/add-velocity target (- (rand) 0.5) 0.9 (- (rand) 0.5))
             (f target))
           (Thread/sleep 1500))
         (.remove entity)))))

(defn quake [entity]
  (something-like-quake
    entity
    LivingEntity
    (fn [_] nil)))

(defn popcorn [entity]
  (something-like-quake
    entity
    Item
    (fn [item]
      (when (= 0 (rand-int 5))
        (.dropItem (.getWorld item) (.getLocation item) (.getItemStack item)))
      (when (= 0 (rand-int 5))
        (.remove item)))))

(defn liquid [material duration entity]
  (let [block (.getBlock (.getLocation entity))]
    (if (= Material/AIR (.getType block))
      (do
        (.setType block material)
        (future-call #(do
                        (Thread/sleep duration)
                        (when (.isLiquid block)
                          (.setType block Material/AIR)))))
      (.sendMessage (.getShooter entity) "failed")))
  (future-call #(do
                  (.dropItem (.getWorld entity) (.getLocation entity) (ItemStack. Material/ARROW))
                  (.remove entity))))

(defn water [entity]
  (liquid Material/WATER 5000 entity))

(defn lava [entity]
  (liquid Material/LAVA 500 entity))

(defn woodbreak [entity]
  (let [block (block-of-arrow entity)
        table {Material/WOODEN_DOOR (repeat 6 (ItemStack. Material/WOOD))
               Material/FENCE (repeat 6 (ItemStack. Material/STICK))
               Material/WALL_SIGN (cons (ItemStack. Material/STICK)
                                        (repeat 6 (ItemStack. Material/WOOD)))
               Material/SIGN_POST (cons (ItemStack. Material/STICK)
                                        (repeat 6 (ItemStack. Material/WOOD)))}
        items (table (.getType block))
        block2 (.getBlock (.getLocation entity))
        items2 (table (.getType block2))]
    (if items
      (do
        (.setType block Material/AIR)
        (doseq [item items]
          (.dropItemNaturally (.getWorld block) (.getLocation block) item)))
      (if items2
        (do
          (.setType block2 Material/AIR)
          (doseq [item items2]
            (.dropItemNaturally (.getWorld block2) (.getLocation block2) item)))
        (.sendMessage (.getShooter entity) "Woodbreak failed."))))
  (.remove entity))

