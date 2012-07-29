(ns cloft.block

    (:import [org.bukkit Material])
    (:require [clojure.set])
    )


(defn category
     ;available options:
     ; :gettable ; may have material from that block with proper tool
     ; :enterable  ; can step in.
     ; :combustible ; can be set fire.
     ; :crafted ; something crafted by player
     ;usage:
     ; (category :gettable :enterable) => #{} of gettable and enterable
     ; (category :gettable ) => #{} of enterable
     ;
     ; http://jd.bukkit.org/apidocs/org/bukkit/Material.html
     ;
     [ & ks]
     (let [c (set ks)
           data {
           Material/AIR #{:enterable}
           Material/WATER #{:enterable} ;flowing can not have it.
           Material/LAVA #{:enterable} ;flowing can not have it
           Material/FIRE #{:enterable}
           Material/STATIONARY_WATER #{:gettable :enterable}
           Material/STATIONARY_LAVA  #{:gettable :enterable}
           Material/WEB #{:gettable :combustible :enterable}
           Material/BROWN_MUSHROOM  #{:gettable :combustible :enterable}
           Material/RED_MUSHROOM #{:gettable :combustible :enterable}
           Material/RED_ROSE #{:gettable :combustible :enterable}
           Material/YELLOW_FLOWER #{:gettable :combustible :enterable}
           Material/WHEAT #{:gettable :combustible :enterable}
           Material/PUMPKIN_STEM #{:gettable :combustible :enterable}
           Material/MELON_STEM #{:gettable :combustible :enterable}
           Material/VINE #{:combustible :enterable}
           Material/GRASS #{:combustible :enterable}
           Material/DEAD_BUSH #{:combustible :enterable} ; FIXME check document for :gettable
           Material/SUGAR_CANE #{:gettable :combustible :enterable}
           Material/LEAVES  #{:gettable :combustible} ; need tool, though.
           Material/MELON_BLOCK #{:gettable :combustible}
           Material/PUMPKIN #{:gettable :combustible}
           Material/WATER_LILY  #{:gettable :enterable}
           Material/BED_BLOCK #{:gettable :combustible :enterable}
           Material/CACTUS #{:gettable :combustible}
           Material/SAND #{:gettable }
           Material/SANDSTONE #{:gettable}
           Material/STONE #{:gettable }
           Material/SOUL_SAND #{:gettable }
           Material/STEP #{:gettable :crafted :enterable}
           Material/STONE_BUTTON #{:gettable :crafted :enterable}
           Material/TNT #{:crafted :gettable }
           Material/WALL_SIGN #{:crafted :enterable}
           Material/SOIL #{:crafted :gettable } ; gettable as dirt
           Material/TORCH #{:crafted :enterable :gettable}
           Material/TRAP_DOOR #{:crafted :enterable :gettable}
           Material/SNOW_BLOCK #{:crafted :gettable}
           Material/SNOW #{:enterable :gettable}
           Material/WOOD #{:combustible :gettable}
           Material/WORKBENCH #{:combustible :crafted :gettable}
           Material/REDSTONE_TORCH_ON #{:enterable :crafted :gettable}
           Material/REDSTONE_TORCH_OFF #{:enterable :crafted :gettable}
           Material/SMOOTH_BRICK #{:crafted :gettable}
           Material/SMOOTH_STAIRS #{:crafted :enterable :gettable}
           Material/PORTAL #{:crafted :enterable}
           Material/NETHER_BRICK #{:crafted :gettable}
           Material/NETHER_BRICK_STAIRS #{:crafted :gettable}
           Material/NETHER_FENCE #{:crafted :gettable}
           Material/NETHER_WARTS #{:gettable}
           Material/MOB_SPAWNER #{:crafted}
           Material/LEVER #{:crafted :enterable :gettable}
           Material/JUKEBOX #{:crafted :gettable}
           Material/LADDER #{:crafted :enterable :gettable}
           Material/FENCE #{:crafted :enterable :gettable}
           Material/FENCE_GATE #{:crafted :enterable :gettable}
           Material/CAULDRON #{:crafted :gettable :enterable}
           Material/PISTON_BASE #{:crafted :gettable} ; as piston
           Material/PISTON_EXTENSION #{:crafted}
           Material/PISTON_MOVING_PIECE #{:crafted}
           Material/PISTON_STICKY_BASE #{:crafted :gettable} ; as sticky piston
           Material/RAILS #{:crafted :gettable}
           Material/SIGN #{:crafted :enterable :gettable}
           Material/WOOD_STAIRS #{:crafted :enterable :combustible :gettable}}]
       (into {} (filter #(clojure.set/subset? c (last %)) data))))
