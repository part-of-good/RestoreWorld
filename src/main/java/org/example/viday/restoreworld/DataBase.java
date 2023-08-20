package org.example.viday.restoreworld;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataBase {
    public static HikariDataSource hds;
    public Connection con;
    private long count = 0;
    StringBuilder stringMaterialID = new StringBuilder();
    private @NotNull BukkitTask async;
    private @NotNull BukkitTask asyncQuery;
    private boolean isStartSetBlock = false;
    private boolean isFinishQuery = false;
    private String stringMaterialIDNotHopper = "";
    private String hopperIDMaterial = "";
    private HashMap<Integer, String> hashMaterials = new HashMap<>();
    private HashMap<Integer, String> hashMetaBlock = new HashMap<>();
    private long minTimeMS = 1687626000;     // МЕНЯТЬ старт по времени (тут у нас указывается минимальное значение отсчета) пример: 1.08.2023
    private long maxTimeMS = 1688230800;     // МЕНЯТЬ максимальный по времени шаг (тут указывается максимальное значение времени) пример: 8.08.2023
    private final int amountStepMS = 604800;   // размер шага в ms (указанное значение это 7 дней - 604800)
    private final int maxStep = 1691790003 / amountStepMS;  // кол-во шагов
    private int currentStep = 0;    // кол-во текущих шагов (для конфига и тайм стопа)
    private final long maxTimeStepMS = 1691790003; // максимум по времени до которого мы можем дойти (11 августа - 1691790003)


    public DataBase(HikariConfig config) {
        hds = new HikariDataSource(config);
        try {
            con = hds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        async = Bukkit.getScheduler().runTaskTimer(RestoreWorld.getInstance(), this::setBlock, 0, 20);
        //getContainerMaterial(); // Сундуки, хоперы и т.д.
        //getContainerBlock();    // после мы вносим корды и состояние контейнеров
        startAsyncQuery();
    }

    private void startAsyncQuery() {
        new BukkitRunnable() {

            @Override
            public void run() {
                BlockDataManager.count = 0;
                if (currentStep < maxStep + 1) {    // +1 ибо деление НЕ на цело может в конечном итоге не дойти до максимального порога времени
                    if (currentStep != 0) {
                        minTimeMS += amountStepMS;              // 0 + 7 = 7
                        maxTimeMS = minTimeMS + amountStepMS;   // 7 + 7 = 14 (для соблюдения интервала)
                    }
                    if (maxTimeMS > maxTimeStepMS) {
                        maxTimeMS = maxTimeStepMS;  // если мы превышаем максимальное значение, то присваиваем максимальное
                    }
                    if (minTimeMS > maxTimeStepMS) {
                        minTimeMS = maxTimeStepMS;  // тоже самое что и выше
                    }
                    // типо работает, наверное, но почему-то превышаю лимит, мб данных мало
                    if (minTimeMS == maxTimeStepMS && maxTimeMS == maxTimeStepMS) {
                        currentStep = maxStep;
                        System.out.println("FINISH! (Кол-во шагов превышают максимальный лимит по времени)");
                    }
                    isFinishQuery = false;
                    currentStep++;
                }
                else {
                    System.out.println("FINALLY FINISH!");
                    async.cancel();
                    isFinishQuery = true;
                    //setItemContainer();
                    return;
                }
                try {
                    RestoreWorld.getInstance().getLogger().info("Формируем запрос...");
                    RestoreWorld.getInstance().getLogger().info("Интервал запроса от " + new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(new Date(Long.parseLong( minTimeMS + "000"))) +
                            " до " + new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(new Date(Long.parseLong( maxTimeMS + "000"))) + " текущий шаг " + currentStep);
                    String query = "SELECT t1.* FROM co_block t1 JOIN (SELECT wid, x, y, z, MAX(time) AS max_time " +
                            "FROM co_block WHERE rolled_back = 0 GROUP BY wid, x, y, z) t2 ON t1.wid = t2.wid " +
                            "AND t1.x = t2.x " +
                            "AND t1.y = t2.y " +
                            "AND t1.z = t2.z " +
                            "AND t1.time = t2.max_time " +
                            "AND t1.time < " + maxTimeMS + " " +
                            "AND t2.max_time < " + maxTimeMS + " " +
                            "AND t1.time > " + minTimeMS + " " +
                            "AND t2.max_time > " + minTimeMS + " " +
                            "ORDER BY t1.time DESC";
                            /*"WHERE t1.type NOT IN (" + stringMaterialID + ") " +
                            "ORDER BY t1.time DESC";*/
                    /*"OR (t1.type = " + hopperIDMaterial + " AND t1.blockdata IS NOT NULL) " +
                    "AND t1.action >= 1";*/

                    //String query = "SELECT t1.* FROM co_block t1 JOIN (SELECT wid, x, y, z, MAX(time) AS max_time FROM co_block WHERE rolled_back = 0 GROUP BY wid, x, y, z) t2 ON t1.wid = t2.wid AND t1.x = t2.x AND t1.y = t2.y AND t1.z = t2.z AND t1.time = t2.max_time AND t1.time < 1691790003 AND t2.max_time < 1691790003 AND t1.time > 1691096400 AND t2.max_time > 1691096400;";
                    //String query = "SELECT t1.* FROM co_block t1 JOIN (SELECT wid, x, y, z, MAX(time) AS max_time FROM co_block WHERE rolled_back = 0 GROUP BY wid, x, y, z) t2 ON t1.wid = t2.wid AND t1.x = t2.x AND t1.y = t2.y AND t1.z = t2.z AND t1.time = t2.max_time";
                    final PreparedStatement stmt = con.prepareStatement(query);
                    ResultSet result = stmt.executeQuery();
                    RestoreWorld.getInstance().getLogger().info("Получили данные! Заносим блоки в массив...");
                    while (result.next()) {
                        String meta = "";
                        if (result.getString("blockdata") != null) {
                            result.getString("blockdata").split(",");
                            String[] dataInt = result.getString("blockdata").split(",");
                            String[] data = new String[dataInt.length];
                            for (int i = 0; i < dataInt.length; i++) {
                                data[i] = getBlockData(Integer.parseInt(dataInt[i]));
                            }
                            meta = String.join(",", data);
                        }
                        // Обработка результатов
                        Location loc = new Location(Bukkit.getWorld(getWorld(result.getInt("wid"))), result.getInt("x"), result.getInt("y"), result.getInt("z"));
                        int type = result.getInt("type");
                        int action = result.getInt("action");
                        int time = result.getInt("time");
                        try {
                            RestoreWorld.getInstance().blockDataManager.addLocationData(loc, meta, getMaterial(type), time);
                        } catch (Exception e) {
                            System.out.println("Ошибка, скип акшиона");
                        }
                        /*if (action >= 1) {
                            RestoreWorld.getInstance().blockDataManager.addLocationData(loc, meta, getMaterial(type), time);
                        }
                        else {
                            RestoreWorld.getInstance().blockDataManager.addLocationData(loc, meta, "air", time);
                        }*/
                    }
                    RestoreWorld.getInstance().getLogger().info("Blocks added: " + BlockDataManager.count);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isFinishQuery = true;
                RestoreWorld.getInstance().getLogger().info("Запуск установки блоков");
                this.cancel();
            }
        }.runTaskAsynchronously(RestoreWorld.getInstance());

        // TODO рабочий вариант отката контейнеров
        //getContainerBlock();    // получаем контейнера
        // Удаление контейнеров если они более не актуальны
        /*System.out.println("Удаление не актуальных контейнеров");
        List<BlockData> blocksToRemove = new ArrayList<>();
        List<ContainerData> containersToRemove = new ArrayList<>();
        for (BlockData blockData : RestoreWorld.getInstance().blockDataManager.getLocationDataList()) {
            for (ContainerData containerData : RestoreWorld.getInstance().containerDataManager.getContainerDataList()) {
                if (blockData.getLocation().equals(containerData.getLocation()) && blockData.getMeta().length() < containerData.getMeta().length() && blockData.getMaterial().equals(containerData.getMaterial())) {
                    System.out.println("Удаления блока из блок даты, т.к. есть контейнер, имеющий поворот");
                    blocksToRemove.add(blockData);
                } else if (blockData.getLocation().equals(containerData.getLocation()) && !blockData.getMaterial().equals(containerData.getMaterial())) {
                    System.out.println("Удаление контейнера, т.к. он был заменен другим блоком");
                    containersToRemove.add(containerData);
                }
            }
        }
        System.out.println("Удаление после итерации");  // Классическое удаление через итераторы сыпет эксепшены
        // Удаление объектов после итерации
        for (BlockData blockData : blocksToRemove) {
            RestoreWorld.getInstance().blockDataManager.removeBlockData(blockData.getLocation(), blockData.getMeta(), blockData.getMaterial(), blockData.getTime());
        }
        for (ContainerData containerData : containersToRemove) {
            RestoreWorld.getInstance().containerDataManager.removeContainerData(containerData.getLocation(), containerData.getMeta(), containerData.getMaterial());
        }
        System.out.println("Удаление завершено");*/
    }
    public void getContainerBlock() {
        // Мы должны получить все id материалов контейнеров, после по полученным значениям мы делаем запрос в бд
        // где получаем все блоки с id, после сортируем их по времени и после оставляем те, у кого есть blockdata
        // Получить wid, x, y, z где type chestID, barrelID (и т.д.), blockdata != 0, далее, если совпадают все значения, то берем где времени больше всего
        // далее мы вносим эти значения в список (новый манагер для контейнеров)
        // В основном запросе игнорируем все контейнера и расставляем как есть
        try {
            String queryContainer = "SELECT t1.* FROM co_block t1 JOIN (SELECT wid, x, y, z, MAX(time) AS max_time " +
                    "FROM co_block WHERE rolled_back = 0 AND blockdata IS NOT NULL GROUP BY wid, x, y, z) t2 ON t1.wid = t2.wid " +
                    "AND t1.x = t2.x " +
                    "AND t1.y = t2.y " +
                    "AND t1.z = t2.z " +
                    "AND t1.time = t2.max_time " +
                    "AND t1.time < " + maxTimeMS + " " +
                    "AND t2.max_time < " + maxTimeMS + " " +
                    "AND t1.time > " + minTimeMS + " " +
                    "AND t2.max_time > " + minTimeMS + " " +
                    "WHERE t1.type IN (" + stringMaterialID + ")";
            PreparedStatement statementContainer = con.prepareStatement(queryContainer);
            ResultSet resultContainer = statementContainer.executeQuery();
            System.out.println("Запуск добавления контейнеров");
            while (resultContainer.next()) {
                try {
                    resultContainer.getString("blockdata").split(",");
                    String[] dataInt = resultContainer.getString("blockdata").split(",");
                    String[] data = new String[dataInt.length];
                    for (int i = 0; i < dataInt.length; i++) {
                        data[i] = getBlockData(Integer.parseInt(dataInt[i]));
                    }
                    String meta = String.join(",", data);
                    int type = resultContainer.getInt("type");
                    Location location = new Location(Bukkit.getWorld(getWorld(resultContainer.getInt("wid"))), resultContainer.getInt("x"), resultContainer.getInt("y"), resultContainer.getInt("z"));
                    RestoreWorld.getInstance().containerDataManager.addLocationData(location, meta, getMaterial(type));
                } catch (Exception e) {
                    RestoreWorld.getInstance().getLogger().warning("Проблема с получением состояния блоков");
                }
            }
            System.out.println("Контейнера добавлены");
            /*System.out.println("Выводим содержимое контейнер базы");
            for (ContainerData containerData : RestoreWorld.getInstance().containerDataManager.getContainerDataList()) {
                System.out.println("Содержимое контейнер базы: " + containerData.getMaterial() + " " + containerData.getMeta() + " " + containerData.getLocation());
            }*/
        } catch (Exception e) {
            RestoreWorld.getInstance().getLogger().warning("EXCEPTION GET CONTAINER!");
            e.printStackTrace();
        }
    }

    public void setBlock(){
        if (!isFinishQuery) {
            System.out.println("Ожидание бд...");
            return;
        }
        if (isStartSetBlock) {
            return;
        }
        isStartSetBlock = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<BlockData> iterator = RestoreWorld.getInstance().blockDataManager.getLocationDataList().iterator();
                int i = 0;
                while (!RestoreWorld.getInstance().blockDataManager.getLocationDataList().isEmpty() && i < 500) {
                    i++;
                    BlockData blockData = iterator.next();
                    try {
                        Location locationData = blockData.getLocation();
                        try {
                            locationData.getBlock().setBlockData(RestoreWorld.getInstance().getServer().createBlockData(blockData.getMaterial() + "[" + blockData.getMeta() + "]"));
                        } catch (Exception e) {
                            //RestoreWorld.getInstance().getLogger().info("Установка блока который не имеет BlockData");
                            locationData.getBlock().setBlockData(RestoreWorld.getInstance().getServer().createBlockData(blockData.getMaterial()));
                        }
                        count++;
                        if (count % 500 == 0) {
                            RestoreWorld.getInstance().getLogger().info("[" + new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(new Date(Long.parseLong( blockData.getTime() + "000"))) + "] " +
                                    "шаг " + currentStep + " [" + Math.round(( ((double) count / 62_000_000) * 100 ) * 1e10) / 1e10 + "%] " +
                                    blockData.getMaterial() + " " +
                                    blockData.getLocation().getBlockX() + " " +
                                    blockData.getLocation().getBlockY() + " " +
                                    blockData.getLocation().getBlockZ());                        }
                        iterator.remove();
                    } catch (Exception e) {
                        iterator.remove();
                        //RestoreWorld.getInstance().getLogger().warning("EXCEPTION!EXCEPTION!EXCEPTION!");
                    }
                }
                isStartSetBlock = false;
                if (RestoreWorld.getInstance().blockDataManager.getLocationDataList().isEmpty()) {
                    System.out.println("Finish! шаг " + currentStep);
                    startAsyncQuery();
                }
                this.cancel();
            }
        }.runTask(RestoreWorld.getInstance());
    }

    public void setItemContainer() {
        // Мы должны брать сундук с актион 1, т.к. это значит, что в сундуке что-то есть
        // Так же, мы должны сортировать контейнера по времени и параметрам wid, x, y, z, type, где type выступает содержимым контейнера (amount кол-во этого предмета)
        // То есть могут быть 10 одинаковых координат, но иметь разное содержимое
        System.out.println("Установка контейнеров");
        Iterator<ContainerData> iterator = RestoreWorld.getInstance().containerDataManager.getContainerDataList().iterator();
        while (!RestoreWorld.getInstance().containerDataManager.getContainerDataList().isEmpty()) {
            ContainerData containerData = iterator.next();
            try {
                Location locationData = containerData.getLocation();
                try {
                    locationData.getBlock().setBlockData(RestoreWorld.getInstance().getServer().createBlockData(containerData.getMaterial() + "[" + containerData.getMeta() + "]"));
                } catch (Exception e) {
                    RestoreWorld.getInstance().getLogger().info("Установка блока который не имеет BlockData");
                    locationData.getBlock().setBlockData(RestoreWorld.getInstance().getServer().createBlockData(containerData.getMaterial()));
                }
                iterator.remove();
            } catch (Exception e) {
                iterator.remove();
                RestoreWorld.getInstance().getLogger().warning("EXCEPTION!EXCEPTION!EXCEPTION!");
            }
        }
        System.out.println("Все контейнера были установлены");
        System.out.println("Запуск добавление вещей в контейнера");
        String query = "SELECT * " +
                "FROM co_container " +
                "WHERE action = 1 " +
                "AND (wid, x, y, z, type, time) IN (" +
                "SELECT wid, x, y, z, type, MAX(time) AS max_time " +
                "FROM co_container " +
                "WHERE action = 1 " +
                "AND time < " + maxTimeStepMS + " " +
                "GROUP BY wid, x, y, z, type " +
                ")";
        try {
            final PreparedStatement stmt = con.prepareStatement(query);
            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                int type = result.getInt("type");
                int amount = result.getInt("amount");
                int wid = result.getInt("wid");
                int x = result.getInt("x");
                int y = result.getInt("y");
                int z = result.getInt("z");
                Location location = new Location(Bukkit.getWorld(getWorld(wid)), x ,y ,z);
                Material blockMaterialContainer = location.getBlock().getType();
                try {
                    switch (blockMaterialContainer) {
                        case CHEST, TRAPPED_CHEST -> {
                            Chest chest = (Chest) location.getBlock().getState();
                            chest.getInventory().addItem(getItem(Material.matchMaterial(getMaterial(type)), amount));
                        }
                        case DISPENSER -> {
                            Dispenser dispenser = (Dispenser) location.getBlock().getState();
                            dispenser.getInventory().addItem(getItem(Material.matchMaterial(getMaterial(type)), amount));
                        }
                        case DROPPER -> {
                            Dropper dropper = (Dropper) location.getBlock().getState();
                            dropper.getInventory().addItem(getItem(Material.matchMaterial(getMaterial(type)), amount));
                        }
                        case HOPPER -> {
                            Hopper hopper = (Hopper) location.getBlock().getState();
                            hopper.getInventory().addItem(getItem(Material.matchMaterial(getMaterial(type)), amount));
                        }
                        case BARREL -> {
                            Barrel barrel = (Barrel) location.getBlock().getState();
                            barrel.getInventory().addItem(getItem(Material.matchMaterial(getMaterial(type)), amount));
                        }
                        default -> {
                            if (location.getBlock().getState() instanceof ShulkerBox) {
                                ShulkerBox shulkerBox = (ShulkerBox) location.getBlock().getState();
                                shulkerBox.getInventory().addItem(getItem(Material.matchMaterial(getMaterial(type)), amount));
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Не удалось получить инвентарь " + blockMaterialContainer);
                    e.printStackTrace();
                }
            }
            System.out.println("Успешно добавили в контейнера реси");
        } catch (Exception e) {
            RestoreWorld.getInstance().getLogger().warning("EXCEPTION CONTAINER!");
        }
        System.out.println("FINALLY FINISH!");
    }

    public ItemStack getItem(Material material, int amount) {
        ItemStack itemStack = new ItemStack(material);  // Получаем итем стак материала
        itemStack.setAmount(amount);                    // Кол-во предметов
        return itemStack;
    }

    public String getWorld(int id){
        try(final PreparedStatement stmt = this.con.prepareStatement("SELECT * FROM co_world WHERE id = ?")) {
            stmt.setInt(1, id);
            try(ResultSet result = stmt.executeQuery()){
                return result.getString("world");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getMaterial(int id){
        try(final PreparedStatement stmt = this.con.prepareStatement("SELECT * FROM co_material_map WHERE id = ?")) {
            stmt.setInt(1, id);
            try(ResultSet result = stmt.executeQuery()){
                return result.getString("material");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getBlockData(int id){
        try(final PreparedStatement stmt = this.con.prepareStatement("SELECT * FROM co_blockdata_map WHERE id = ?")) {
            stmt.setInt(1, id);
            try(ResultSet result = stmt.executeQuery()){
                return result.getString("data");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getContainerMaterial() {
        System.out.println("Хэшируем материалы контейнера");
        // TODO 100% можно сделать лучше
        HashMap<String, Integer> hashMaterialContainer = new HashMap<>();
        String query = "SELECT id, material FROM co_material_map";
        try (final PreparedStatement statement = con.prepareStatement(query)) {
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                int id = result.getInt("id");
                String material = result.getString("material");
                // Получаем материалы
                if (material.equals("minecraft:chest") ||
                        material.equals("minecraft:trapped_chest") ||
                        material.equals("minecraft:barrel") ||
                        material.equals("minecraft:dispenser") ||
                        material.equals("minecraft:dropper") ||
                        material.equals("minecraft:hopper")) {
                    System.out.println(material);
                    hashMaterialContainer.put(material, id);
                }
                if (material.equals("minecraft:hopper")) {
                    hopperIDMaterial = String.valueOf(id);
                }
            }
            // Проходим по всем элементам хэш мапы и добавляем их в строку
            for (HashMap.Entry<String, Integer> entry : hashMaterialContainer.entrySet()) {
                if (stringMaterialID.length() > 0) {
                    stringMaterialID.append(",");
                }
                stringMaterialID.append(entry.getValue());
            }

            // Разделяем строку на массив строк по запятым (нужно для строки без id хопера)
            String[] parts = String.valueOf(stringMaterialID).split(",");
            List<String> modifiedParts = new ArrayList<>();
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num != hashMaterialContainer.get("minecraft:hopper")) {
                    modifiedParts.add(part);
                }
            }
            // Объединяем подстроки обратно в строку с использованием запятых
            stringMaterialIDNotHopper = String.join(",", modifiedParts);

            System.out.println("Хеширование завершено");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}