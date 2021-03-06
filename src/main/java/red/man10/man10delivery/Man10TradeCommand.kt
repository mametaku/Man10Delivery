package red.man10.man10delivery


import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class Man10TradeCommand(internal var plugin: Man10Delivery) : CommandExecutor {
    val tradePair = ArrayList<Pair<Player, Player>>()
    val sendMoney = HashMap<Player, Int>()
    val isFinished = HashMap<Player, Boolean>()
    val sendItem = HashMap<Player,ArrayList<ItemStack>>()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {

        if (sender !is Player) {
            return true
        }

        if (!sender.hasPermission("mtrade.use")) {
            sender.sendMessage(plugin.prefix + "§cあなたには権限がありません！")
            return true
        }

        if (args[1] == null) {

            sender.sendMessage(plugin.prefix + "§eトレードをするプレイヤーを入力してください")

            return true
        }

        //////////////
        //permission check
        if (!sender.hasPermission("mtrade." + args[0])) {
            sender.sendMessage(plugin.prefix + "§cあなたには権限がありません！")
            return true

        }

        if (args[0] == "accept") {
            for (player in tradePair) {
                if (player == sender) {
                    openInventory(player.first, player.second)
                    return true
                }
            }

            sender.sendMessage(plugin.prefix + "§eあなたにトレード申請をした人はいません")
            return true
        }

        if (!Bukkit.getPlayer(args[0])?.isOnline!!) {
            sender.sendMessage(plugin.prefix + "§eトレードする相手の名前が間違えてる、もしくはオフラインです")
            return true
        }
        for (player in tradePair) {
            if (player == sender) {
                sender.sendMessage(plugin.prefix + "§eあなたは既に他のプレイヤーにトレード申請をしています")
            }
        }

        checkTrade(Bukkit.getPlayer(args[0]), sender)


        return true
    }

    private fun checkTrade(p: Player?, pair: Player) {

        if (p == pair){
            pair.sendMessage(plugin.prefix + "自分とトレードはできません")
        }

        tradePair.add(Pair(pair, p) as Pair<Player, Player>)

        Bukkit.getScheduler().runTask(plugin,Runnable{

            p?.sendMessage(plugin.prefix + "§e§l" + pair.name + "§r§eからトレード申請が来ています！")
            p?.sendMessage(plugin.prefix + "§e§l/mtrade accept (30秒以上経過すると、キャンセルされます)")

            try {
                Thread.sleep(3000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
                Bukkit.getLogger().info(e.message)
            }


        })

    }

    private fun openInventory(p1: Player, p2: Player) {
        val inv = Bukkit.createInventory(null, 54, "§e§lトレード")

        val stack = ItemStack(Material.GLASS_PANE, 1)
        val meta = stack.itemMeta
        meta.setDisplayName("")
        stack.itemMeta = meta

        val index = intArrayOf(5, 14, 23, 32, 41, 50)

        for (i in index) {
            inv.setItem(i, stack)
        }

        stack.type = Material.LIME_STAINED_GLASS_PANE
        meta.setDisplayName("§a§l完了")
        stack.itemMeta = meta
        inv.setItem(46, stack)
        inv.setItem(47, stack)

        stack.type = Material.RED_STAINED_GLASS_PANE
        meta.setDisplayName("§4§l取引中止")
        stack.itemMeta = meta
        inv.setItem(48, stack)
        inv.setItem(49, stack)

        stack.type = Material.ORANGE_STAINED_GLASS_PANE
        meta.setDisplayName("§l相手は取引完了していません")
        stack.itemMeta = meta
        inv.setItem(51, stack)
        inv.setItem(52, stack)
        inv.setItem(53, stack)
        inv.setItem(54, stack)


        stack.type = Material.BLAZE_POWDER
        meta.setDisplayName("§a§l+10,000$")
        stack.itemMeta = meta
        inv.setItem(40, stack)

        stack.type = Material.GREEN_DYE
        meta.setDisplayName("§a§l+100,000$")
        stack.itemMeta = meta
        inv.setItem(39, stack)

        stack.type = Material.BLUE_DYE
        meta.setDisplayName("§a§l+1,000,000$")
        stack.itemMeta = meta
        inv.setItem(38, stack)

        stack.type = Material.YELLOW_DYE
        meta.setDisplayName("§a§l+10,000,000$")
        stack.itemMeta = meta
        inv.setItem(37, stack)


        stack.type = Material.OAK_SIGN
        meta.setDisplayName("§lあなたが支払う金額:§e§l0$")
        stack.itemMeta = meta
        inv.setItem(43, stack)

        meta.setDisplayName("§l相手が支払う金額：§e§l0$")
        stack.itemMeta = meta
        inv.setItem(44, stack)

        meta.setDisplayName(p2.name+"")
        inv.setItem(45, stack)
        p1.openInventory(inv)

        meta.setDisplayName(p1.name+"")
        inv.setItem(45, stack)

        p2.openInventory(inv)

    }

    fun addMoney(player: Player, level: Int) {
        when (level) {
            37 -> {
                if (plugin.vault.getBalance(player.uniqueId) <= 10000) return
                sendMoney[player]!!.plus(10000)
            }
            38 -> {
                if (plugin.vault.getBalance(player.uniqueId) <= 100000) return
                sendMoney[player]!!.plus(100000)
            }
            39 -> {
                if (plugin.vault.getBalance(player.uniqueId) <= 1000000) return
                sendMoney[player]!!.plus(1000000)
            }
            40 -> {
                if (plugin.vault.getBalance(player.uniqueId) <= 10000000) return
                sendMoney[player]!!.plus(10000000)
            }
        }

        var i = player.inventory
        val stack = ItemStack(Material.OAK_SIGN)
        val meta = stack.itemMeta
        meta.setDisplayName("§lあなたが支払う金額:§e§l${sendMoney[player]}$")
        stack.itemMeta = meta

        i.setItem(43,stack)
        player.openInventory(i)

        i = getPair(player).inventory
        meta.setDisplayName("§l相手が支払う金額：§e§l${sendMoney[player]}$")
        stack.itemMeta = meta
        i.setItem(44,stack)

        getPair(player).openInventory(i)


    }

    fun clickFinish(player: Player){
        isFinished[player] = true

        val inv = getPair(player).inventory
        val stack = ItemStack(Material.LIME_STAINED_GLASS_PANE,1)
        val meta = stack.itemMeta
        meta.setDisplayName("§a§l相手は完了を押しました")
        stack.itemMeta = meta
        inv.setItem(51,stack)
        inv.setItem(52,stack)
        inv.setItem(53,stack)
        inv.setItem(54,stack)

    }

    fun addItem(player: Player, send: ItemStack?){
        if (send != null) {
            sendItem[player]!!.add(send)
        }

        val inv = getPair(player).inventory
        inv.addItem(send)
        getPair(player).openInventory(inv)
    }

    fun removeItem(player: Player,remove:ItemStack){
        sendItem[player]!!.remove(remove)

        val inv = getPair(player).inventory
        inv.remove(remove)
        getPair(player).openInventory(inv)
    }

    fun finish(player: Player){
        val pair = getPair(player)

        player.closeInventory()
        pair.closeInventory()

        isFinished.remove(player)
        isFinished.remove(pair)

        for (i in sendItem[player]!!){
            getPair(player).inventory.addItem(i)
        }
        for (i in sendItem[pair]!!){
            player.inventory.addItem(i)
        }
        sendItem.remove(player)
        sendItem.remove(pair)
        plugin.vault.givePlayerMoney(player.uniqueId,sendMoney[pair]!!.toDouble(),null,"mtrade")
        plugin.vault.givePlayerMoney(getPair(player).uniqueId,sendMoney[pair]!!.toDouble(),null,"mtrade")

        sendMoney.remove(player)
        sendMoney.remove(pair)

        tradePair.remove(Pair(player,pair))
    }



    fun getPair(p:Player):Player{
        return Bukkit.getPlayer(p.inventory.getItem(45)?.itemMeta!!.displayName)!!
    }

}