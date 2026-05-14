    }

    public void stopWave(Arena arena) {
        cancelTasks(arena);
        removeBossBar(arena);
    }

    public Optional<Wave> getWave(int number) {
        return Optional.ofNullable(waves.get(number));
    }

    private void spawnEntries(Arena arena, Wave wave) {
        wave.setSpawningStarted(true);
        BossBar bossBar = getBossBar(arena);
        bossBar.setColor(BarColor.RED);
        bossBar.setProgress(1.0);
        bossBar.setTitle("Wave " + wave.getNumber() + " is running");

        long delay = 0L;
        for (WaveMobEntry entry : wave.getEntries()) {
            for (int i = 0; i < entry.getAmount(); i++) {
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (arena.isRunning()) {
                        mobManager.spawnMob(arena, entry.getType());
                        wave.incrementSpawnedMobs();
                    }
                }, delay);
                trackTask(arena, task);
                delay += Math.max(1, entry.getSpawnDelayTicks());
            }
        }
    }

    private void startNextWaveIfPresent(Arena arena, int currentNumber) {
        Optional<Integer> nextWave = waves.keySet().stream()
                .filter(number -> number > currentNumber)
                .min(Comparator.naturalOrder());
        if (nextWave.isPresent()) {
            startWave(arena, nextWave.get());
        } else {
            BossBar bossBar = getBossBar(arena);
            bossBar.setColor(BarColor.GREEN);
            bossBar.setProgress(1.0);
            bossBar.setTitle("All waves completed");
        }
    }

    private Wave copyWave(Wave wave) {
        return new Wave(wave.getNumber(), wave.getEntries(), wave.getStartDelayTicks());
    }

    private void trackTask(Arena arena, BukkitTask task) {
        activeTasks.computeIfAbsent(arena.getId().toLowerCase(), ignored -> new ArrayList<>()).add(task);
    }

    private void cancelTasks(Arena arena) {
        List<BukkitTask> tasks = activeTasks.remove(arena.getId().toLowerCase());
        if (tasks == null) {
            return;
        }
        for (BukkitTask task : tasks) {
            task.cancel();
        }
    }

    private void startCountdownBossBar(Arena arena, Wave wave) {
        BossBar bossBar = getBossBar(arena);
        bossBar.setColor(BarColor.YELLOW);
        bossBar.setStyle(BarStyle.SOLID);
        addArenaPlayers(arena, bossBar);

        int totalTicks = Math.max(1, wave.getStartDelayTicks());
        BukkitTask countdownTask = new BukkitRunnable() {
            private int remainingTicks = totalTicks;

            @Override
            public void run() {
                if (!arena.isRunning() || !wave.isActive()) {
                    cancel();
                    return;
                }
                if (remainingTicks <= 0) {
                    cancel();
                    return;
                }
                addArenaPlayers(arena, bossBar);
                double seconds = remainingTicks / 20.0;
                bossBar.setTitle("Next wave " + wave.getNumber() + " in " + String.format("%.1f", seconds) + "s");
                bossBar.setProgress(Math.max(0.0, Math.min(1.0, remainingTicks / (double) totalTicks)));
                remainingTicks--;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        trackTask(arena, countdownTask);
    }

    private void updateActiveWaveBossBar(Arena arena, Wave wave) {
        BossBar bossBar = getBossBar(arena);
        addArenaPlayers(arena, bossBar);
        int alive = mobManager.getLivingMobs(arena).size();
        int remainingToSpawn = Math.max(0, wave.getTotalMobs() - wave.getSpawnedMobs());
        int remaining = alive + remainingToSpawn;
        bossBar.setColor(BarColor.RED);
        bossBar.setTitle("Wave " + wave.getNumber() + " | Mobs left: " + remaining);
        bossBar.setProgress(wave.getTotalMobs() <= 0 ? 0.0 : Math.max(0.0, Math.min(1.0, remaining / (double) wave.getTotalMobs())));
    }

    private BossBar getBossBar(Arena arena) {
        return bossBars.computeIfAbsent(arena.getId().toLowerCase(), ignored ->
                Bukkit.createBossBar("Tower Defense", BarColor.YELLOW, BarStyle.SOLID));
    }

    private void addArenaPlayers(Arena arena, BossBar bossBar) {
        for (Player player : arena.getWorld().getPlayers()) {
            if (!bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
            }
        }
        for (Player player : new ArrayList<>(bossBar.getPlayers())) {
            if (!player.getWorld().equals(arena.getWorld())) {
                bossBar.removePlayer(player);
            }
        }
    }

    private void removeBossBar(Arena arena) {
        BossBar bossBar = bossBars.remove(arena.getId().toLowerCase());
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }
}
