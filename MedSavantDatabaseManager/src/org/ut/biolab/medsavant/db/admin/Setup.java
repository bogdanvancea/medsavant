package org.ut.biolab.medsavant.db.admin;

import com.healthmarketscience.sqlbuilder.InsertQuery;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.UserTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.DBSettings;
import org.ut.biolab.medsavant.db.util.DBUtil;
import org.ut.biolab.medsavant.db.util.query.UserQueryUtil;

/**
 *
 * @author mfiume
 */
public class Setup {

    private static void dropTables(Connection c) throws SQLException {

        if (DBUtil.tableExists( MedSavantDatabase.UserTableSchema.getTablename())) {
            List<String> userNames = getValuesFromField(c,MedSavantDatabase.UserTableSchema.getTablename(), "name");
            for (String s : userNames) {
                UserQueryUtil.removeUser(s);
            }
        }

        if (DBUtil.tableExists( MedSavantDatabase.PatienttablemapTableSchema.getTablename())) {
            List<String> patientTables = getValuesFromField(c,MedSavantDatabase.PatienttablemapTableSchema.getTablename(), "patient_tablename");
            for (String s : patientTables) {
                DBUtil.dropTable(s);
            }
        }
        
        if (DBUtil.tableExists( MedSavantDatabase.VarianttablemapTableSchema.getTablename())) {
            List<String> variantTables = getValuesFromField(c,MedSavantDatabase.VarianttablemapTableSchema.getTablename(), "variant_tablename");
            for (String s : variantTables) {
                DBUtil.dropTable(s);
            }
        }

        DBUtil.dropTable(MedSavantDatabase.ServerlogTableSchema.getTablename());
        DBUtil.dropTable(MedSavantDatabase.UserTableSchema.getTablename());
        DBUtil.dropTable(MedSavantDatabase.AnnotationTableSchema.getTablename());
        DBUtil.dropTable(MedSavantDatabase.ReferenceTableSchema.getTablename());
        DBUtil.dropTable(MedSavantDatabase.ProjectTableSchema.getTablename());
        DBUtil.dropTable(MedSavantDatabase.PatienttablemapTableSchema.getTablename());
        DBUtil.dropTable(MedSavantDatabase.VarianttablemapTableSchema.getTablename());
        DBUtil.dropTable(MedSavantDatabase.RegionsetTableSchema.getTablename());
        DBUtil.dropTable(MedSavantDatabase.RegionsetmembershipTableSchema.getTablename());
        DBUtil.dropTable(MedSavantDatabase.CohortTableSchema.getTablename());
        DBUtil.dropTable(MedSavantDatabase.CohortmembershipTableSchema.getTablename());
        DBUtil.dropTable(MedSavantDatabase.VariantpendingupdateTableSchema.getTablename());
        DBUtil.dropTable(MedSavantDatabase.PatienttablemapTableSchema.getTablename());
        DBUtil.dropTable(MedSavantDatabase.ChromosomeTableSchema.getTablename());
        DBUtil.dropTable(MedSavantDatabase.PatientformatTableSchema.getTablename());
        DBUtil.dropTable(MedSavantDatabase.AnnotationformatTableSchema.getTablename());
        DBUtil.dropTable(MedSavantDatabase.AlignmentTableSchema.getTablename());
    }

    private static void createTables(Connection c) throws SQLException {

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.ServerlogTableSchema.getTablename() + "` ("
                  + "`id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                  + "`user` varchar(50) COLLATE latin1_bin DEFAULT NULL,"
                  + "`event` varchar(50) COLLATE latin1_bin DEFAULT NULL,"
                  + "`description` blob,"
                  + "`timestamp` datetime NOT NULL,"
                  + "PRIMARY KEY (`id`)"
                + ") ENGINE=MyISAM;"
                );
        
        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.UserTableSchema.getTablename() + "` ("
                + "`id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`name` varchar(50) COLLATE latin1_bin NOT NULL DEFAULT '',"
                + "`is_admin` tinyint(1) NOT NULL DEFAULT '0',"
                + "PRIMARY KEY (`id`),"
                + "UNIQUE KEY `name` (`name`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.RegionsetTableSchema.getTablename() + "` ("
                + "`region_set_id` int(11) NOT NULL AUTO_INCREMENT,"
                + "`name` varchar(255) CHARACTER SET latin1 NOT NULL,"
                + "PRIMARY KEY (`region_set_id`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.RegionsetmembershipTableSchema.getTablename() + "` ("
                + "`regionset_id` int(11) NOT NULL,"
                + "`genome_id` int(11) NOT NULL,"
                + "`chrom` varchar(255) COLLATE latin1_bin NOT NULL,"
                + "`start` int(11) NOT NULL,"
                + "`end` int(11) NOT NULL,"
                + "`description` varchar(255) COLLATE latin1_bin NOT NULL"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.CohortTableSchema.getTablename() + "` ("
                + "`cohort_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`project_id` int(11) unsigned NOT NULL,"
                + "`name` varchar(255) CHARACTER SET latin1 NOT NULL,"
                + "PRIMARY KEY (`cohort_id`,`project_id`) USING BTREE"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.CohortmembershipTableSchema.getTablename() + "` ("
                + "`cohort_id` int(11) unsigned NOT NULL,"
                + "`patient_id` int(11) unsigned NOT NULL,"
                + "PRIMARY KEY (`patient_id`,`cohort_id`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.ReferenceTableSchema.getTablename() + "` ("
                + "`reference_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`name` varchar(50) COLLATE latin1_bin NOT NULL,"
                + "PRIMARY KEY (`reference_id`), "
                + "UNIQUE KEY `name` (`name`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.AnnotationTableSchema.getTablename() + "` ("
                + "`annotation_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`program` varchar(100) COLLATE latin1_bin NOT NULL DEFAULT '',"
                + "`version` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`reference_id` int(11) unsigned NOT NULL,"
                + "`path` varchar(500) COLLATE latin1_bin NOT NULL DEFAULT '',"
                + "`has_ref` tinyint(1) NOT NULL,"
                + "`has_alt` tinyint(1) NOT NULL,"
                + "`type` int(11) unsigned NOT NULL,"
                + "PRIMARY KEY (`annotation_id`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.ProjectTableSchema.getTablename() + "` "
                + "(`project_id` int(11) unsigned NOT NULL AUTO_INCREMENT, "
                + "`name` varchar(50) NOT NULL, "
                + "PRIMARY KEY (`project_id`), "
                + "UNIQUE KEY `name` (`name`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.PatienttablemapTableSchema.getTablename() + "` ("
                + "`project_id` int(11) unsigned NOT NULL,"
                + "`patient_tablename` varchar(100) COLLATE latin1_bin NOT NULL,"
                + "PRIMARY KEY (`project_id`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.VarianttablemapTableSchema.getTablename() + "` ("
                + "`project_id` int(11) unsigned NOT NULL,"
                + "`reference_id` int(11) unsigned NOT NULL,"
                + "`variant_tablename` varchar(100) COLLATE latin1_bin NOT NULL,"
                + "`annotation_ids` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "UNIQUE KEY `unique` (`project_id`,`reference_id`,`variant_tablename`)"
                + ") ENGINE=MyISAM;");
        
        c.createStatement().execute(
                "CREATE TABLE  `" + MedSavantDatabase.VariantpendingupdateTableSchema.getTablename() + "` ("
                + "`update_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`project_id` int(11) unsigned NOT NULL,"
                + "`reference_id` int(11) unsigned NOT NULL,"
                + "`action` int(11) unsigned NOT NULL,"
                + "`status` int(5) unsigned NOT NULL DEFAULT '0',"
                + "`timestamp` datetime DEFAULT NULL,"
                + "PRIMARY KEY (`update_id`) USING BTREE"
                + ") ENGINE=MyISAM;");
        
        c.createStatement().execute(
                "CREATE TABLE  `" + MedSavantDatabase.ChromosomeTableSchema.getTablename() + "` ("
                + "`reference_id` int(11) unsigned NOT NULL,"
                + "`contig_id` int(11) unsigned NOT NULL,"
                + "`contig_name` varchar(100) COLLATE latin1_bin NOT NULL,"
                + "`contig_length` int(11) unsigned NOT NULL,"
                + "`centromere_pos` int(11) unsigned NOT NULL,"
                + "PRIMARY KEY (`reference_id`,`contig_id`) USING BTREE"
                +") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");
        
        c.createStatement().execute(
                "CREATE TABLE  `" + MedSavantDatabase.AnnotationformatTableSchema.getTablename() + "` ("
                + "`annotation_id` int(11) unsigned NOT NULL,"
                + "`position` int(11) unsigned NOT NULL,"
                + "`column_name` varchar(200) COLLATE latin1_bin NOT NULL,"
                + "`column_type` varchar(45) COLLATE latin1_bin NOT NULL,"
                + "`filterable` tinyint(1) NOT NULL,"
                + "`alias` varchar(200) COLLATE latin1_bin NOT NULL,"
                + "`description` varchar(500) COLLATE latin1_bin NOT NULL,"
                + "PRIMARY KEY (`annotation_id`,`position`)"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");
        
        c.createStatement().execute(
                "CREATE TABLE  `" + MedSavantDatabase.PatientformatTableSchema.getTablename() + "` ("
                + "`project_id` int(11) unsigned NOT NULL,"
                + "`position` int(11) unsigned NOT NULL,"
                + "`column_name` varchar(200) COLLATE latin1_bin NOT NULL,"
                + "`column_type` varchar(45) COLLATE latin1_bin NOT NULL,"
                + "`filterable` tinyint(1) NOT NULL,"
                + "`alias` varchar(200) COLLATE latin1_bin NOT NULL,"
                + "`description` varchar(500) COLLATE latin1_bin NOT NULL,"
                + "PRIMARY KEY (`project_id`,`position`)"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");
        
        c.createStatement().execute(
                "CREATE TABLE  `default_patient` ("
                + "`patient_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`family_id` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`pedigree_id` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`hospital_id` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`dna_ids` varchar(1000) COLLATE latin1_bin DEFAULT NULL,"
                + "PRIMARY KEY (`patient_id`)"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");
        
        c.createStatement().execute(
                "CREATE TABLE  `default_variant` ("
                + "`upload_id` int(11) NOT NULL,"
                + "`file_id` int(11) NOT NULL,"
                + "`variant_id` int(11) NOT NULL,"
                + "`dna_id` varchar(100) COLLATE latin1_bin NOT NULL,"
                + "`chrom` varchar(5) COLLATE latin1_bin NOT NULL DEFAULT '',"
                + "`position` int(11) NOT NULL,"
                + "`dbsnp_id` varchar(45) COLLATE latin1_bin DEFAULT NULL,"
                + "`ref` varchar(30) COLLATE latin1_bin DEFAULT NULL,"
                + "`alt` varchar(30) COLLATE latin1_bin DEFAULT NULL,"
                + "`qual` float(10,0) DEFAULT NULL,"
                + "`filter` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`aa` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`ac` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`af` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`an` int(11) DEFAULT NULL,"
                + "`bq` float DEFAULT NULL,"
                + "`cigar` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`db` int(1) DEFAULT NULL,"
                + "`dp` int(11) DEFAULT NULL,"
                + "`end` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`h2` int(1) DEFAULT NULL,"
                + "`mq` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`mq0` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`ns` int(11) DEFAULT NULL,"
                + "`sb` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`somatic` int(1) DEFAULT NULL,"
                + "`validated` int(1) DEFAULT NULL,"
                + "`custom_info` varchar(500) COLLATE latin1_bin DEFAULT NULL"
                + ") ENGINE=BRIGHTHOUSE DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");
        
        c.createStatement().execute(
                "CREATE TABLE  `" + MedSavantDatabase.AlignmentTableSchema.getTablename() + "` ("
                + "`dna_id` varchar(100) COLLATE latin1_bin NOT NULL,"
                + "`bam_file` varchar(5000) COLLATE latin1_bin NOT NULL,"
                + "PRIMARY KEY (`dna_id`)"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");
    }
    
    private static void addRootUser(Connection c) throws SQLException {
        
        TableSchema table = MedSavantDatabase.UserTableSchema;
        InsertQuery query = new InsertQuery(table.getTable());
        query.addColumn(table.getDBColumn(UserTableSchema.COLUMNNAME_OF_NAME), "root");
        query.addColumn(table.getDBColumn(UserTableSchema.COLUMNNAME_OF_IS_ADMIN), 1);
        
        c.createStatement().executeUpdate(query.toString());
    }

    public static boolean createDatabase(String dbhost, int port, String dbname) {
        
        try {
            Connection c = ConnectionController.connectUnpooled(dbhost, port, "");
            createDatabase(c,dbname);
            
            /*
            ConnectionController.setDbhost(dbhost);
            ConnectionController.setPort(port);
            ConnectionController.setDbname(dbname);
             */
            
            c = ConnectionController.connectUnpooled(dbhost, port,dbname);
            
            ConnectionController.setDbhost(dbhost);
            ConnectionController.setPort(port);
            ConnectionController.setDbname(dbname);
            
            dropTables(c);
            createTables(c);
            addRootUser(c);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
    public static void main(String[] args) throws SQLException {
        createDatabase("localhost",5029,"test2");
    }

    private static List<String> getValuesFromField(Connection c,String tablename, String fieldname) throws SQLException {
        String q = "SELECT `" + fieldname + "` FROM `" + tablename + "`";
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery(q);
        List<String> results = new ArrayList<String>();
        while (rs.next()) {
            results.add(rs.getString(1));
        }
        return results;
    }

    private static void createDatabase(Connection c, String dbname) throws SQLException {
        //TODO: should check if the db exists already
        c.createStatement().execute("CREATE DATABASE " + dbname);
    }
}
