package app.patterns.resource_pool.task;

interface DbConnection {
    void execute(String query);
    void close();
}
