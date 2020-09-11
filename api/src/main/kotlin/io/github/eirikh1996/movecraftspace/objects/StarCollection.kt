package io.github.eirikh1996.movecraftspace.objects

import io.github.eirikh1996.movecraftspace.Settings
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.PrintWriter


object StarCollection : Iterable<Star> {
    val stars = HashSet<Star>()
    lateinit var pl : Plugin
    override fun iterator(): Iterator<Star> {
        return stars.iterator()
    }

    /**
     * @param loc The location from which to calculate from
     * @param maxDistance The maximum distance between given loc and closest star. -1 equals no maximum distance
     */
    fun closestStar(loc : Location, maxDistance : Int = -1) : Star? {
        var star : Star? = null
        var lastDistance = if (maxDistance <= -1) Double.MAX_VALUE else maxDistance.toDouble()
        for (s in StarCollection) {
            val distance = s.loc.distance(ImmutableVector.fromLocation(loc))
            if (s.space.equals(loc.world) && distance <= lastDistance) {
                star = s
                lastDistance = distance
            }

        }
        return star
    }

    fun add(s : Star) {
        stars.add(s)
        saveFile()
    }

    fun remove(s : Star) {
        stars.remove(s)
        saveFile()
    }

    fun loadStars() {
        val file = File(PlanetCollection.pl.dataFolder, "stars.yml")
        if (!file.exists())
            return
        val yaml = Yaml()
        val data = yaml.load<Map<String, Any>>(FileInputStream(file))
        val planetData = data.get("stars") as Map<String, Any>
        for (entry in planetData.entries) {
            val entryData = entry.value as ArrayList<Any>
            val space = Bukkit.getWorld(entryData[0] as String)
            if (space == null)
                continue
            stars.add(Star(entry.key, space, ImmutableVector(entryData[1] as Int , entryData[2] as Int, entryData[3] as Int)))
        }
        pl.logger.info("Loaded " + stars.size + " stars")
    }

    private fun saveFile() {
        if (stars.isEmpty())
            return
        val file = File(pl.dataFolder, "stars.yml")
        if (!file.exists())
            file.createNewFile()
        val writer = PrintWriter(file)
        writer.println("stars:")
        for (star in stars) {
            writer.println("   " + star.name + ": [" + star.space.name + ", " + star.loc.x + ", " + star.loc.y + ", " + star.loc.z + "]")
        }
        writer.close()
    }

    val asStringList : List<String> get() {
        val list = ArrayList<String>()
        for (star in this)
            list.add(star.name)
        return list
    }

    fun getStarByName(name : String): Star? {
        for (star in this) {
            if (!star.name.equals(name, true))
                continue
            return star
        }
        return null
    }
}