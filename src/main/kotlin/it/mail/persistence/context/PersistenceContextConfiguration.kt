package it.mail.persistence.context

import it.mail.persistence.common.IdGenerator
import it.mail.persistence.common.LocalCounterIdGenerator
import it.mail.persistence.jdbc.JdbcMailMessageRepository
import it.mail.persistence.jdbc.JdbcMailMessageTypeRepository
import org.apache.commons.dbutils.QueryRunner
import javax.inject.Singleton
import javax.sql.DataSource

class PersistenceContextConfiguration {

    @Singleton
    fun queryRunner() = QueryRunner()

    @Singleton
    fun idGenerator() = LocalCounterIdGenerator()
}

@Singleton
class RepositoriesConfiguration(
    private val idGenerator: IdGenerator,
    private val dataSource: DataSource,
    private val queryRunner: QueryRunner,
) {

    @Singleton
    fun mailMessageTypeRepository() = JdbcMailMessageTypeRepository(idGenerator, dataSource, queryRunner)

    @Singleton
    fun mailMessageRepository() = JdbcMailMessageRepository(idGenerator, dataSource, queryRunner)
}
