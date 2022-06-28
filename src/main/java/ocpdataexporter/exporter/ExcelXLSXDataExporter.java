package ocpdataexporter.exporter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class ExcelXLSXDataExporter extends DataExporter {

    private class SheetData {
        private Sheet sheet;
        private int[] columnsWidth;
        private int currentRowIndex;

        private boolean namespaceStart;

        public SheetData(Sheet sheet, int[] columnsWidth, int currentRowIndex) {
            this.sheet = sheet;
            this.columnsWidth = columnsWidth;
            this.currentRowIndex = currentRowIndex;
            this.namespaceStart = false;
        }

        public SheetData(Sheet sheet, int[] columnsWidth) {
            this(sheet, columnsWidth, -1);
        }

        public int nextRowIndex() {
            if (currentRowIndex < 0)
                currentRowIndex = 0;
            else {
                currentRowIndex++;
            }
            return currentRowIndex;
        }

        public void markNamespaceStart() {
            namespaceStart = true;
        }

        public void unmarkNamespaceStart() {
            namespaceStart = false;
        }

        public boolean isNamespaceStart() {
            return namespaceStart;
        }
    }

    private static final int BASE_COLUMN_WIDTH = 5000;
    private static final int S_COLUMN_WIDTH = BASE_COLUMN_WIDTH;
    private static final int SM_COLUMN_WIDTH = (int) Math.round(BASE_COLUMN_WIDTH*1.5);
    private static final int M_COLUMN_WIDTH = BASE_COLUMN_WIDTH*2;
    private static final int ML_COLUMN_WIDTH = (int) Math.round(BASE_COLUMN_WIDTH*2.5);
    private static final int L_COLUMN_WIDTH = BASE_COLUMN_WIDTH*3;
    private static final int LXL_COLUMN_WIDTH = (int) Math.round(BASE_COLUMN_WIDTH*3.5);
    private static final int XL_COLUMN_WIDTH = BASE_COLUMN_WIDTH*4;
    private static final int XLXXL_COLUMN_WIDTH = (int) Math.round(BASE_COLUMN_WIDTH*4.5);
    private static final int XXL_COLUMN_WIDTH = BASE_COLUMN_WIDTH*5;

    private static final int XXXL_COLUMN_WIDTH = BASE_COLUMN_WIDTH*6;
    private static final int XXXXL_COLUMN_WIDTH = BASE_COLUMN_WIDTH*7;
    private static final int XXXXXL_COLUMN_WIDTH = BASE_COLUMN_WIDTH*8;


    private static final int NAMESPACES_SHEET = 0;
    private static final int WORKLOADS_SHEET = 1;
    private static final int SERVICES_SHEET = 2;
    private static final int PVC_SHEET = 3;
    private static final int BATCH_WORKLOADS_SHEET = 4;
    private static final int HPA_SHEET = 5;
    private static final int BUILD_CONFIG_SHEET = 6;
    private static final int IMAGE_STREAM_SHEET = 7;
    private SheetData[] sheetData = new SheetData[8];

    private Workbook workbook;

    private CellStyle headerStyle;
    private CellStyle namespaceListRowStyle;
    private CellStyle namespaceRowStyle;
    private CellStyle deploymentRowStyle;
    private CellStyle deploymentRowStyleNSStart;
    private CellStyle statefulSetRowStyle;
    private CellStyle statefulSetRowStyleNSStart;
    private CellStyle daemonSetRowStyle;
    private CellStyle daemonSetRowStyleNSStart;
    private CellStyle containerRowStyle;

    private CellStyle serviceRowStyle;
    private CellStyle serviceRowStyleNSStart;

    private CellStyle pvcRowStyle;
    private CellStyle pvcRowStyleNSStart;

    private CellStyle batchRowStyle;
    private CellStyle batchRowStyleNSStart;

    private CellStyle hpaRowStyle;
    private CellStyle hpaRowStyleNSStart;

    private CellStyle buildConfigRowStyle;
    private CellStyle buildConfigRowStyleNSStart;

    private CellStyle imageStreamRowStyle;
    private CellStyle imageStreamRowStyleNSStart;

    private String outputFullFilepath;

    public ExcelXLSXDataExporter(String outputFullFilepath) {
        this.outputFullFilepath = outputFullFilepath;
        workbook = new XSSFWorkbook();
        sheetData[NAMESPACES_SHEET] = new SheetData(
                workbook.createSheet("namespaces"),
                new int[] {
                        M_COLUMN_WIDTH,  // Namespace
                        S_COLUMN_WIDTH,  // Workload count
                        XXXL_COLUMN_WIDTH // Data
                });
        sheetData[WORKLOADS_SHEET] = new SheetData(
                workbook.createSheet("workloads"),
                new int[] {
                        M_COLUMN_WIDTH,  // Namespace
                        M_COLUMN_WIDTH,  // Kind
                        M_COLUMN_WIDTH,  // Name
                        S_COLUMN_WIDTH,  // Strategy Type
                        S_COLUMN_WIDTH,  // Replicas
                        S_COLUMN_WIDTH,  // Has Affinity
                        S_COLUMN_WIDTH,  // Use Helm
                        LXL_COLUMN_WIDTH, // Helm Data
                        L_COLUMN_WIDTH,  // Selectors
                        L_COLUMN_WIDTH,  // Services name/type/(ports)
                        LXL_COLUMN_WIDTH,  // Configmaps
                        LXL_COLUMN_WIDTH,  // Secrets
                        LXL_COLUMN_WIDTH,  // PVCs
                        LXL_COLUMN_WIDTH,  // Other volumes
                        M_COLUMN_WIDTH,  // Security Context
                        S_COLUMN_WIDTH,  // Restart Policy
                        S_COLUMN_WIDTH,  // Container Count
                        M_COLUMN_WIDTH,  // Container Name
                        M_COLUMN_WIDTH,  // Ports
                        S_COLUMN_WIDTH,  // CPU Requests
                        S_COLUMN_WIDTH,  // Mem Requests
                        S_COLUMN_WIDTH,  // CPU Limits
                        S_COLUMN_WIDTH,  // Mem Limits
                        S_COLUMN_WIDTH,  // Liveness Probe
                        S_COLUMN_WIDTH,  // Readiness Probe
                        S_COLUMN_WIDTH,  // Image pull policy
                        XXXXXL_COLUMN_WIDTH // Image
                });
        sheetData[SERVICES_SHEET] = new SheetData(
                workbook.createSheet("services"),
                new int[] {
                        M_COLUMN_WIDTH,
                        M_COLUMN_WIDTH,
                        M_COLUMN_WIDTH,
                        L_COLUMN_WIDTH,
                        L_COLUMN_WIDTH,
                        L_COLUMN_WIDTH,    // External IPs
                        XXXL_COLUMN_WIDTH // Routes
                });
        sheetData[PVC_SHEET] = new SheetData(
                workbook.createSheet("pvc"),
                new int[] {
                        M_COLUMN_WIDTH,
                        M_COLUMN_WIDTH,
                        S_COLUMN_WIDTH,
                        S_COLUMN_WIDTH,
                        S_COLUMN_WIDTH,
                        M_COLUMN_WIDTH,
                        S_COLUMN_WIDTH,
                        L_COLUMN_WIDTH
                });
        sheetData[BATCH_WORKLOADS_SHEET] = new SheetData(
                workbook.createSheet("jobs"),
                new int[] {
                        M_COLUMN_WIDTH,  // Namespace
                        M_COLUMN_WIDTH,  // Kind
                        M_COLUMN_WIDTH,  // Name
                        S_COLUMN_WIDTH,  // Backoff Limit
                        S_COLUMN_WIDTH,  // Active Deadline secs
                        S_COLUMN_WIDTH,  // Parallelism
                        S_COLUMN_WIDTH,  // TTL secs After Finished
                        S_COLUMN_WIDTH,  // Completions
                        M_COLUMN_WIDTH,  // Schedule
                        M_COLUMN_WIDTH,  // Security Context
                        M_COLUMN_WIDTH,  // Restart Policy
                        S_COLUMN_WIDTH,  // Container Count
                        M_COLUMN_WIDTH,  // Container Name
                        M_COLUMN_WIDTH,  // Ports
                        S_COLUMN_WIDTH,  // CPU Requests
                        S_COLUMN_WIDTH,  // Mem Requests
                        S_COLUMN_WIDTH,  // CPU Limits
                        S_COLUMN_WIDTH,  // Mem Limits
                        S_COLUMN_WIDTH,  // Liveness Probe
                        S_COLUMN_WIDTH,  // Readiness Probe
                        S_COLUMN_WIDTH,  // Image pull policy
                        XXXXXL_COLUMN_WIDTH // Image
                });
        sheetData[HPA_SHEET] = new SheetData(
                workbook.createSheet("hpa"),
                new int[] {
                        M_COLUMN_WIDTH, // Namespace
                        M_COLUMN_WIDTH, // Name
                        M_COLUMN_WIDTH, // Target Kind
                        M_COLUMN_WIDTH, // Target Name
                        S_COLUMN_WIDTH, // Min replica
                        S_COLUMN_WIDTH, // Max Replica
                        S_COLUMN_WIDTH  // Target CPU Utilization %
                });
        sheetData[BUILD_CONFIG_SHEET] = new SheetData(
                workbook.createSheet("buildconfig"),
                new int[] {
                        M_COLUMN_WIDTH,   // Namespace
                        M_COLUMN_WIDTH,   // Name
                        S_COLUMN_WIDTH,   // Source Type
                        LXL_COLUMN_WIDTH, // Source Data
                        S_COLUMN_WIDTH,   // Strategy Type
                        S_COLUMN_WIDTH,   // Output Type
                        LXL_COLUMN_WIDTH  // Output Data
                });
        sheetData[IMAGE_STREAM_SHEET] = new SheetData(
                workbook.createSheet("imagestream"),
                new int[] {
                        M_COLUMN_WIDTH,   // Namespace
                        M_COLUMN_WIDTH,   // Name
                        LXL_COLUMN_WIDTH, // Tags
                });

        headerStyle = workbook.createCellStyle();
        headerStyle.setLocked(true);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont fontHeader = ((XSSFWorkbook) workbook).createFont();
        fontHeader.setFontName("Arial");
        fontHeader.setColor(IndexedColors.WHITE.getIndex());
        fontHeader.setFontHeightInPoints((short) 12);
        fontHeader.setBold(true);
        headerStyle.setFont(fontHeader);
        headerStyle.setVerticalAlignment(VerticalAlignment.TOP);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        XSSFFont fontNormal = ((XSSFWorkbook) workbook).createFont();
        fontNormal.setFontName("Arial");
        fontNormal.setColor(IndexedColors.WHITE.getIndex());
        fontNormal.setFontHeightInPoints((short) 12);

        XSSFFont fontNormalDark = ((XSSFWorkbook) workbook).createFont();
        fontNormalDark.setFontName("Arial");
        fontNormalDark.setColor(IndexedColors.BLACK.getIndex());
        fontNormalDark.setFontHeightInPoints((short) 12);

        namespaceListRowStyle = workbook.createCellStyle();
        namespaceListRowStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        namespaceListRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        namespaceListRowStyle.setFont(fontNormalDark);
        namespaceListRowStyle.setVerticalAlignment(VerticalAlignment.TOP);
        namespaceListRowStyle.setWrapText(true);
        namespaceListRowStyle.setBorderBottom(BorderStyle.THIN);

        namespaceRowStyle = workbook.createCellStyle();
        namespaceRowStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        namespaceRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        namespaceRowStyle.setFont(fontNormal);
        namespaceRowStyle.setVerticalAlignment(VerticalAlignment.TOP);
        namespaceRowStyle.setBorderTop(BorderStyle.MEDIUM);
        namespaceRowStyle.setBorderBottom(BorderStyle.THIN);

        deploymentRowStyle = workbook.createCellStyle();
        deploymentRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        deploymentRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        deploymentRowStyle.setFont(fontNormalDark);
        deploymentRowStyle.setVerticalAlignment(VerticalAlignment.TOP);
        deploymentRowStyle.setWrapText(true);
        deploymentRowStyle.setBorderBottom(BorderStyle.THIN);

        statefulSetRowStyle = workbook.createCellStyle();
        statefulSetRowStyle.cloneStyleFrom(deploymentRowStyle);

        daemonSetRowStyle = workbook.createCellStyle();
        daemonSetRowStyle.cloneStyleFrom(deploymentRowStyle);

        batchRowStyle = workbook.createCellStyle();
        batchRowStyle.cloneStyleFrom(deploymentRowStyle);

        deploymentRowStyleNSStart = workbook.createCellStyle();
        deploymentRowStyleNSStart.cloneStyleFrom(deploymentRowStyle);
        deploymentRowStyleNSStart.setBorderTop(BorderStyle.THICK);

        statefulSetRowStyleNSStart = workbook.createCellStyle();
        statefulSetRowStyleNSStart.cloneStyleFrom(statefulSetRowStyle);
        statefulSetRowStyleNSStart.setBorderTop(BorderStyle.THICK);

        daemonSetRowStyleNSStart = workbook.createCellStyle();
        daemonSetRowStyleNSStart.cloneStyleFrom(daemonSetRowStyle);
        daemonSetRowStyleNSStart.setBorderTop(BorderStyle.THICK);

        batchRowStyleNSStart = workbook.createCellStyle();
        batchRowStyleNSStart.cloneStyleFrom(batchRowStyle);
        batchRowStyleNSStart.setBorderTop(BorderStyle.THICK);

        containerRowStyle = workbook.createCellStyle();
        containerRowStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        containerRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        containerRowStyle.setFont(fontNormalDark);
        containerRowStyle.setVerticalAlignment(VerticalAlignment.TOP);
        containerRowStyle.setWrapText(true);
        containerRowStyle.setBorderBottom(BorderStyle.THIN);

        serviceRowStyle = workbook.createCellStyle();
        serviceRowStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        serviceRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        serviceRowStyle.setFont(fontNormalDark);
        serviceRowStyle.setVerticalAlignment(VerticalAlignment.TOP);
        serviceRowStyle.setWrapText(true);
        serviceRowStyle.setBorderBottom(BorderStyle.THIN);

        serviceRowStyleNSStart = workbook.createCellStyle();
        serviceRowStyleNSStart.cloneStyleFrom(serviceRowStyle);
        serviceRowStyleNSStart.setBorderTop(BorderStyle.THICK);

        pvcRowStyle = workbook.createCellStyle();
        pvcRowStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        pvcRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        pvcRowStyle.setFont(fontNormalDark);
        pvcRowStyle.setVerticalAlignment(VerticalAlignment.TOP);
        pvcRowStyle.setWrapText(true);
        pvcRowStyle.setBorderBottom(BorderStyle.THIN);

        pvcRowStyleNSStart = workbook.createCellStyle();
        pvcRowStyleNSStart.cloneStyleFrom(pvcRowStyle);
        pvcRowStyleNSStart.setBorderTop(BorderStyle.THICK);

        hpaRowStyle = workbook.createCellStyle();
        hpaRowStyle.cloneStyleFrom(pvcRowStyle);

        hpaRowStyleNSStart = workbook.createCellStyle();
        hpaRowStyleNSStart.cloneStyleFrom(hpaRowStyle);
        hpaRowStyleNSStart.setBorderTop(BorderStyle.THICK);

        buildConfigRowStyle = workbook.createCellStyle();
        buildConfigRowStyle.cloneStyleFrom(pvcRowStyle);

        buildConfigRowStyleNSStart = workbook.createCellStyle();
        buildConfigRowStyleNSStart.cloneStyleFrom(buildConfigRowStyle);
        buildConfigRowStyleNSStart.setBorderTop(BorderStyle.THICK);

        imageStreamRowStyle = workbook.createCellStyle();
        imageStreamRowStyle.cloneStyleFrom(pvcRowStyle);

        imageStreamRowStyleNSStart = workbook.createCellStyle();
        imageStreamRowStyleNSStart.cloneStyleFrom(imageStreamRowStyle);
        imageStreamRowStyleNSStart.setBorderTop(BorderStyle.THICK);
    }

    private Cell cellString(Row row, int column, String value, CellStyle style) {
        return cell(row, column, value, CellType.STRING, style);
    }

    private Cell cellNumeric(Row row, int column, int value, CellStyle style) {
        return cell(row, column, "" + value, CellType.NUMERIC, style);
    }

    private Cell cellNumericLong(Row row, int column, long value, CellStyle style) {
        return cell(row, column, "" + value, CellType.NUMERIC, style);
    }

    private Cell cell(Row row, int column, String value, CellType type, CellStyle style) {
        Cell cell = row.createCell(column, type);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return cell;
    }

    private void addRowPadding(Row row, int startColumn, int endColumn, CellStyle style) {
        for (int c = startColumn; c <= endColumn; c++) {
            cellString(row, c, "", style);
        }
    }

    @Override
    public void exportHeaderData() {
        Row headerRow = sheetData[NAMESPACES_SHEET].sheet.createRow(sheetData[NAMESPACES_SHEET].nextRowIndex());
        int headerColumnIndex = 0;
        for (String headerColumn : namespacesHeaderFields) {
            sheetData[NAMESPACES_SHEET].sheet.setColumnWidth(headerColumnIndex, sheetData[NAMESPACES_SHEET].columnsWidth[headerColumnIndex]);
            cell(headerRow, headerColumnIndex, headerColumn, CellType.STRING, headerStyle);
            headerColumnIndex++;
        }
        sheetData[NAMESPACES_SHEET].sheet.createFreezePane(0, 1);

        headerRow = sheetData[WORKLOADS_SHEET].sheet.createRow(sheetData[WORKLOADS_SHEET].nextRowIndex());
        headerColumnIndex = 0;
        for (String headerColumn : workloadsHeaderFields) {
            sheetData[WORKLOADS_SHEET].sheet.setColumnWidth(headerColumnIndex, sheetData[WORKLOADS_SHEET].columnsWidth[headerColumnIndex]);
            cell(headerRow, headerColumnIndex, headerColumn, CellType.STRING, headerStyle);
            headerColumnIndex++;
        }
        sheetData[WORKLOADS_SHEET].sheet.createFreezePane(0, 1);

        headerRow = sheetData[SERVICES_SHEET].sheet.createRow(sheetData[SERVICES_SHEET].nextRowIndex());
        headerColumnIndex = 0;
        for (String headerColumn : servicesHeaderFields) {
            sheetData[SERVICES_SHEET].sheet.setColumnWidth(headerColumnIndex, sheetData[SERVICES_SHEET].columnsWidth[headerColumnIndex]);
            cell(headerRow, headerColumnIndex, headerColumn, CellType.STRING, headerStyle);
            headerColumnIndex++;
        }
        sheetData[SERVICES_SHEET].sheet.createFreezePane(0, 1);

        headerRow = sheetData[PVC_SHEET].sheet.createRow(sheetData[PVC_SHEET].nextRowIndex());
        headerColumnIndex = 0;
        for (String headerColumn : pvcHeaderFields) {
            sheetData[PVC_SHEET].sheet.setColumnWidth(headerColumnIndex, sheetData[PVC_SHEET].columnsWidth[headerColumnIndex]);
            cell(headerRow, headerColumnIndex, headerColumn, CellType.STRING, headerStyle);
            headerColumnIndex++;
        }
        sheetData[PVC_SHEET].sheet.createFreezePane(0, 1);

        headerRow = sheetData[BATCH_WORKLOADS_SHEET].sheet.createRow(sheetData[BATCH_WORKLOADS_SHEET].nextRowIndex());
        headerColumnIndex = 0;
        for (String headerColumn : batchWorkloadsHeaderFields) {
            sheetData[BATCH_WORKLOADS_SHEET].sheet.setColumnWidth(headerColumnIndex, sheetData[BATCH_WORKLOADS_SHEET].columnsWidth[headerColumnIndex]);
            cell(headerRow, headerColumnIndex, headerColumn, CellType.STRING, headerStyle);
            headerColumnIndex++;
        }
        sheetData[BATCH_WORKLOADS_SHEET].sheet.createFreezePane(0, 1);

        headerRow = sheetData[HPA_SHEET].sheet.createRow(sheetData[HPA_SHEET].nextRowIndex());
        headerColumnIndex = 0;
        for (String headerColumn : hpaHeaderFields) {
            sheetData[HPA_SHEET].sheet.setColumnWidth(headerColumnIndex, sheetData[HPA_SHEET].columnsWidth[headerColumnIndex]);
            cell(headerRow, headerColumnIndex, headerColumn, CellType.STRING, headerStyle);
            headerColumnIndex++;
        }
        sheetData[HPA_SHEET].sheet.createFreezePane(0, 1);

        headerRow = sheetData[BUILD_CONFIG_SHEET].sheet.createRow(sheetData[BUILD_CONFIG_SHEET].nextRowIndex());
        headerColumnIndex = 0;
        for (String headerColumn : buildConfigHeaderFields) {
            sheetData[BUILD_CONFIG_SHEET].sheet.setColumnWidth(headerColumnIndex, sheetData[BUILD_CONFIG_SHEET].columnsWidth[headerColumnIndex]);
            cell(headerRow, headerColumnIndex, headerColumn, CellType.STRING, headerStyle);
            headerColumnIndex++;
        }
        sheetData[BUILD_CONFIG_SHEET].sheet.createFreezePane(0, 1);

        headerRow = sheetData[IMAGE_STREAM_SHEET].sheet.createRow(sheetData[IMAGE_STREAM_SHEET].nextRowIndex());
        headerColumnIndex = 0;
        for (String headerColumn : imageStreamHeaderFields) {
            sheetData[IMAGE_STREAM_SHEET].sheet.setColumnWidth(headerColumnIndex, sheetData[IMAGE_STREAM_SHEET].columnsWidth[headerColumnIndex]);
            cell(headerRow, headerColumnIndex, headerColumn, CellType.STRING, headerStyle);
            headerColumnIndex++;
        }
        sheetData[IMAGE_STREAM_SHEET].sheet.createFreezePane(0, 1);
    }

    @Override
    public void exportNamespace(String namespace, int workloadCount, String[] namespaceData) {
        Row row = sheetData[NAMESPACES_SHEET].sheet.createRow(sheetData[NAMESPACES_SHEET].nextRowIndex());
        int columnIndex = 0;
        cellString(row, columnIndex++, namespace, namespaceListRowStyle);
        cellNumeric(row, columnIndex++, workloadCount, namespaceListRowStyle);
        cellString(row, columnIndex++, stringArraySerializer(namespaceData, "\n"), namespaceListRowStyle);
    }

    @Override
    public void exportNamespaceWorkloadData(String namespace) {
        sheetData[WORKLOADS_SHEET].markNamespaceStart();

//        Row row = sheetData[WORKLOADS_SHEET].sheet.createRow(sheetData[WORKLOADS_SHEET].nextRowIndex());
//        int columnIndex = 0;
//        cellString(row, columnIndex++, namespace, namespaceRowStyle);
//
//        addRowPadding(row, columnIndex++, workloadsHeaderFields.length - 1, namespaceRowStyle);
    }

    @Override
    public void exportNamespaceBatchWorkloadData(String namespace) {
        sheetData[BATCH_WORKLOADS_SHEET].markNamespaceStart();
    }

    @Override
    public void exportNamespaceServiceData(String namespace) {
        sheetData[SERVICES_SHEET].markNamespaceStart();
    }

    @Override
    public void exportNamespacePVCData(String namespace) {
        sheetData[PVC_SHEET].markNamespaceStart();
    }

    @Override
    public void exportNamespaceHPAData(String namespace) {
        sheetData[HPA_SHEET].markNamespaceStart();
    }

    @Override
    public void exportNamespaceBuildConfigData(String namespace) {
        sheetData[BUILD_CONFIG_SHEET].markNamespaceStart();
    }

    @Override
    public void exportNamespaceImageStreamData(String namespace) {
        sheetData[IMAGE_STREAM_SHEET].markNamespaceStart();
    }

    @Override
    public void exportDeploymentData(String namespace, String dKind, String dName, String dStrategy, int dReplicas, String dHasAffinity, String dUseHelm, String[] dHelmData, Map<String, String> dSelectors, String[] dMatchedServices, String[] dConfigmaps, String[] dSecrets, String[] dPvcs, String[] dOtherVolumes, String[] dSecurityContextValues, String dRestartPolicy, int dContainerCount) {
        CellStyle style = deploymentRowStyle;
        if (sheetData[WORKLOADS_SHEET].isNamespaceStart()) {
            style = deploymentRowStyleNSStart;
            sheetData[WORKLOADS_SHEET].unmarkNamespaceStart();
        }

        Row row = sheetData[WORKLOADS_SHEET].sheet.createRow(sheetData[WORKLOADS_SHEET].nextRowIndex());
        int columnIndex = 0;
        cellString(row, columnIndex++, namespace, style);
        cellString(row, columnIndex++, dKind, style);
        cellString(row, columnIndex++, dName, style);
        cellString(row, columnIndex++, dStrategy, style);
        cellNumeric(row, columnIndex++, dReplicas, style);
        cellString(row, columnIndex++, dHasAffinity, style);
        cellString(row, columnIndex++, dUseHelm, style);
        cellString(row, columnIndex++, stringArraySerializer(dHelmData, "\n"), style);
        cellString(row, columnIndex++, stringStringMapSerializer(dSelectors, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dMatchedServices, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dConfigmaps, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dSecrets, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dPvcs, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dOtherVolumes, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dSecurityContextValues, "\n"), style);
        cellString(row, columnIndex++, dRestartPolicy, style);
        cellNumeric(row, columnIndex++, dContainerCount, style);

        addRowPadding(row, columnIndex, workloadsHeaderFields.length-1, style);
    }

    @Override
    public void exportDeploymentConfigData(String namespace, String dKind, String dName, String dStrategy, int dReplicas, String dHasAffinity, String dUseHelm, String[] dHelmData, Map<String, String> dSelectors, String[] dMatchedServices, String[] dConfigmaps, String[] dSecrets, String[] dPvcs, String[] dOtherVolumes, String[] dSecurityContextValues, String dRestartPolicy, int dContainerCount) {
        CellStyle style = deploymentRowStyle;
        if (sheetData[WORKLOADS_SHEET].isNamespaceStart()) {
            style = deploymentRowStyleNSStart;
            sheetData[WORKLOADS_SHEET].unmarkNamespaceStart();
        }

        Row row = sheetData[WORKLOADS_SHEET].sheet.createRow(sheetData[WORKLOADS_SHEET].nextRowIndex());
        int columnIndex = 0;
        cellString(row, columnIndex++, namespace, style);
        cellString(row, columnIndex++, dKind, style);
        cellString(row, columnIndex++, dName, style);
        cellString(row, columnIndex++, dStrategy, style);
        cellNumeric(row, columnIndex++, dReplicas, style);
        cellString(row, columnIndex++, dHasAffinity, style);
        cellString(row, columnIndex++, dUseHelm, style);
        cellString(row, columnIndex++, stringArraySerializer(dHelmData, "\n"), style);
        cellString(row, columnIndex++, stringStringMapSerializer(dSelectors, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dMatchedServices, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dConfigmaps, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dSecrets, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dPvcs, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dOtherVolumes, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dSecurityContextValues, "\n"), style);
        cellString(row, columnIndex++, dRestartPolicy, style);
        cellNumeric(row, columnIndex++, dContainerCount, style);

        addRowPadding(row, columnIndex, workloadsHeaderFields.length-1, style);
    }

    @Override
    public void exportStatefulSetData(String namespace, String sKind, String sName, String sStrategy, int sReplicas, String sHasAffinity, String sUseHelm, String[] sHelmData, Map<String, String> sSelectors, String[] sMatchedServices, String[] sConfigmaps, String[] sSecrets, String[] sPvcs, String[] sOtherVolumes, String[] sSecurityContextValues, String sRestartPolicy, int sContainerCount) {
        CellStyle style = statefulSetRowStyle;
        if (sheetData[WORKLOADS_SHEET].isNamespaceStart()) {
            style = statefulSetRowStyleNSStart;
            sheetData[WORKLOADS_SHEET].unmarkNamespaceStart();
        }

        Row row = sheetData[WORKLOADS_SHEET].sheet.createRow(sheetData[WORKLOADS_SHEET].nextRowIndex());
        int columnIndex = 0;
        cellString(row, columnIndex++, namespace, style);
        cellString(row, columnIndex++, sKind, style);
        cellString(row, columnIndex++, sName, style);
        cellString(row, columnIndex++, sStrategy, style);
        cellNumeric(row, columnIndex++, sReplicas, style);
        cellString(row, columnIndex++, sHasAffinity, style);
        cellString(row, columnIndex++, sUseHelm, style);
        cellString(row, columnIndex++, stringArraySerializer(sHelmData, "\n"), style);
        cellString(row, columnIndex++, stringStringMapSerializer(sSelectors, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(sMatchedServices, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(sConfigmaps, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(sSecrets, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(sPvcs, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(sOtherVolumes, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(sSecurityContextValues, "\n"), style);
        cellString(row, columnIndex++, sRestartPolicy, style);
        cellNumeric(row, columnIndex++, sContainerCount, style);

        addRowPadding(row, columnIndex, workloadsHeaderFields.length-1, style);
    }

    @Override
    public void exportDaemonSetData(String namespace, String dKind, String dName, String dStrategy, int dReplicas, String dHasAffinity, String dUseHelm, String[] dHelmData, Map<String, String> dSelectors, String[] dMatchedServices, String[] dConfigmaps, String[] dSecrets, String[] dPvcs, String[] dOtherVolumes, String[] dSecurityContextValues, String dRestartPolicy, int dContainerCount) {
        CellStyle style = daemonSetRowStyle;
        if (sheetData[WORKLOADS_SHEET].isNamespaceStart()) {
            style = daemonSetRowStyleNSStart;
            sheetData[WORKLOADS_SHEET].unmarkNamespaceStart();
        }

        Row row = sheetData[WORKLOADS_SHEET].sheet.createRow(sheetData[WORKLOADS_SHEET].nextRowIndex());
        int columnIndex = 0;
        cellString(row, columnIndex++, namespace, style);
        cellString(row, columnIndex++, dKind, style);
        cellString(row, columnIndex++, dName, style);
        cellString(row, columnIndex++, dStrategy, style);
        cellNumeric(row, columnIndex++, dReplicas, style);
        cellString(row, columnIndex++, dHasAffinity, style);
        cellString(row, columnIndex++, dUseHelm, style);
        cellString(row, columnIndex++, stringArraySerializer(dHelmData, "\n"), style);
        cellString(row, columnIndex++, stringStringMapSerializer(dSelectors, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dMatchedServices, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dConfigmaps, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dSecrets, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dPvcs, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dOtherVolumes, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(dSecurityContextValues, "\n"), style);
        cellString(row, columnIndex++, dRestartPolicy, style);
        cellNumeric(row, columnIndex++, dContainerCount, style);

        addRowPadding(row, columnIndex, workloadsHeaderFields.length-1, style);
    }

    @Override
    public void exportJobData(String namespace, String bKind, String bName, String bBackoffLimit, String bActiveDeadlineSeconds, String bParallelism, String bTTLSecondsAfterFinished, String bCompletions, String bSchedule, String[] bSecurityContextValues, String bRestartPolicy, int bContainerCount) {
        CellStyle style = batchRowStyle;
        if (sheetData[BATCH_WORKLOADS_SHEET].isNamespaceStart()) {
            style = batchRowStyleNSStart;
            sheetData[BATCH_WORKLOADS_SHEET].unmarkNamespaceStart();
        }

        Row row = sheetData[BATCH_WORKLOADS_SHEET].sheet.createRow(sheetData[BATCH_WORKLOADS_SHEET].nextRowIndex());
        int columnIndex = 0;
        cellString(row, columnIndex++, namespace, style);
        cellString(row, columnIndex++, bKind, style);
        cellString(row, columnIndex++, bName, style);
        cellString(row, columnIndex++, bBackoffLimit, style);
        cellString(row, columnIndex++, bActiveDeadlineSeconds, style);
        cellString(row, columnIndex++, bParallelism, style);
        cellString(row, columnIndex++, bTTLSecondsAfterFinished, style);
        cellString(row, columnIndex++, bCompletions, style);
        cellString(row, columnIndex++, bSchedule, style);
        cellString(row, columnIndex++, stringArraySerializer(bSecurityContextValues, "\n"), style);
        cellString(row, columnIndex++, bRestartPolicy, style);
        cellNumeric(row, columnIndex++, bContainerCount, style);

        addRowPadding(row, columnIndex, batchWorkloadsHeaderFields.length-1, style);
    }

    @Override
    public void exportContainerData(WorkloadType workloadType, String namespace, int totalContainerCount, String cName, String cImagePullPolicy, String cImage, String[] cPorts, String cpuReq, String memReq, String cpuLim, String memLim, String livenessProbe, String readinessProbe) { //, String[] cSecurityContextValues) {
        int sheetId = WorkloadType.WORKLOAD.equals(workloadType) ? WORKLOADS_SHEET : BATCH_WORKLOADS_SHEET;
        Row row = sheetData[sheetId].sheet.createRow(sheetData[sheetId].nextRowIndex());

        cellString(row, 0, namespace, containerRowStyle);
        cellString(row, 1, "Container", containerRowStyle);
        int columnIndex;

        columnIndex = WorkloadType.WORKLOAD.equals(workloadType) ? 16 : 11; // WORKLOAD (deployment, dc, statefulset, daemonset) or BATCH WORKLOAD

        addRowPadding(row, 2, columnIndex-1, containerRowStyle);
        cellNumeric(row, columnIndex++, totalContainerCount, containerRowStyle);
        cellString(row, columnIndex++, cName, containerRowStyle);
        cellString(row, columnIndex++, stringArraySerializer(cPorts, "\n"), containerRowStyle);
        cellString(row, columnIndex++, cpuReq, containerRowStyle);
        cellString(row, columnIndex++, memReq, containerRowStyle);
        cellString(row, columnIndex++, cpuLim, containerRowStyle);
        cellString(row, columnIndex++, memLim, containerRowStyle);
        cellString(row, columnIndex++, livenessProbe, containerRowStyle);
        cellString(row, columnIndex++, readinessProbe, containerRowStyle);
        cellString(row, columnIndex++, cImagePullPolicy, containerRowStyle);
        cellString(row, columnIndex++, cImage, containerRowStyle);
//        cellString(row, columnIndex++, stringArraySerializer(cSecurityContextValues, "\n"), containerRowStyle);

        addRowPadding(row, columnIndex, (WorkloadType.WORKLOAD.equals(workloadType) ? workloadsHeaderFields.length : batchWorkloadsHeaderFields.length)-1, containerRowStyle);
    }

    @Override
    public void exportServiceData(String namespace, String sName, String sType, Map<String, String> sSelectors, String[] sPorts, String[] externalIPsArray, String[] sRoutes) {
        CellStyle style = serviceRowStyle;
        if (sheetData[WORKLOADS_SHEET].isNamespaceStart()) {
            style = serviceRowStyleNSStart;
            sheetData[WORKLOADS_SHEET].unmarkNamespaceStart();
        }

        Row row = sheetData[SERVICES_SHEET].sheet.createRow(sheetData[SERVICES_SHEET].nextRowIndex());

        int columnIndex = 0;
        cellString(row, columnIndex++, namespace, style);
        cellString(row, columnIndex++, sName, style);
        cellString(row, columnIndex++, sType, style);
        cellString(row, columnIndex++, stringStringMapSerializer(sSelectors, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(sPorts, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(externalIPsArray, "\n"), style);
        cellString(row, columnIndex++, stringArraySerializer(sRoutes, "\n"), style);

        addRowPadding(row, columnIndex, servicesHeaderFields.length-1, style);
    }

    @Override
    public void exportPVCData(String namespace, String name, String status, String capacity, String accessMode, String storageClassName, String volumeMode, String volumeName) {
        CellStyle style = pvcRowStyle;
        if (sheetData[WORKLOADS_SHEET].isNamespaceStart()) {
            style = pvcRowStyleNSStart;
            sheetData[WORKLOADS_SHEET].unmarkNamespaceStart();
        }

        Row row = sheetData[PVC_SHEET].sheet.createRow(sheetData[PVC_SHEET].nextRowIndex());

        int columnIndex = 0;
        cellString(row, columnIndex++, namespace, style);
        cellString(row, columnIndex++, name, style);
        cellString(row, columnIndex++, status, style);
        cellString(row, columnIndex++, capacity, style);
        cellString(row, columnIndex++, accessMode, style);
        cellString(row, columnIndex++, storageClassName, style);
        cellString(row, columnIndex++, volumeMode, style);
        cellString(row, columnIndex++, volumeName, style);

        addRowPadding(row, columnIndex, pvcHeaderFields.length-1, style);
    }

    @Override
    public void exportHPAData(String namespace, String name, String targetKind, String targetName, int minReplica, int maxReplica, int targetCPUUtilizationPercentage) {
        CellStyle style = hpaRowStyle;
        if (sheetData[HPA_SHEET].isNamespaceStart()) {
            style = hpaRowStyleNSStart;
            sheetData[HPA_SHEET].unmarkNamespaceStart();
        }

        Row row = sheetData[HPA_SHEET].sheet.createRow(sheetData[HPA_SHEET].nextRowIndex());

        int columnIndex = 0;
        cellString(row, columnIndex++, namespace, style);
        cellString(row, columnIndex++, name, style);
        cellString(row, columnIndex++, targetKind, style);
        cellString(row, columnIndex++, targetName, style);
        cellNumeric(row, columnIndex++, minReplica, style);
        cellNumeric(row, columnIndex++, maxReplica, style);
        cellNumeric(row, columnIndex++, targetCPUUtilizationPercentage, style);

        addRowPadding(row, columnIndex, hpaHeaderFields.length-1, style);
    }

    @Override
    public void exportBuildConfigData(String namespace, String name, String sourceType, String[] sourceData, String strategyType, String outputType, String outputData) {
        CellStyle style = buildConfigRowStyle;
        if (sheetData[BUILD_CONFIG_SHEET].isNamespaceStart()) {
            style = buildConfigRowStyleNSStart;
            sheetData[BUILD_CONFIG_SHEET].unmarkNamespaceStart();
        }

        Row row = sheetData[BUILD_CONFIG_SHEET].sheet.createRow(sheetData[BUILD_CONFIG_SHEET].nextRowIndex());

        int columnIndex = 0;
        cellString(row, columnIndex++, namespace, style);
        cellString(row, columnIndex++, name, style);
        cellString(row, columnIndex++, sourceType, style);
        cellString(row, columnIndex++, stringArraySerializer(sourceData, "\n"), style);
        cellString(row, columnIndex++, strategyType, style);
        cellString(row, columnIndex++, outputType, style);
        cellString(row, columnIndex++, outputData, style);

        addRowPadding(row, columnIndex, buildConfigHeaderFields.length-1, style);
    }

    @Override
    public void exportImageStreamData(String namespace, String name, String[] tags) {
        CellStyle style = imageStreamRowStyle;
        if (sheetData[IMAGE_STREAM_SHEET].isNamespaceStart()) {
            style = imageStreamRowStyleNSStart;
            sheetData[IMAGE_STREAM_SHEET].unmarkNamespaceStart();
        }

        Row row = sheetData[IMAGE_STREAM_SHEET].sheet.createRow(sheetData[IMAGE_STREAM_SHEET].nextRowIndex());

        int columnIndex = 0;
        cellString(row, columnIndex++, namespace, style);
        cellString(row, columnIndex++, name, style);
        cellString(row, columnIndex++, stringArraySerializer(tags, "\n"), style);

        addRowPadding(row, columnIndex, imageStreamHeaderFields.length-1, style);
    }

    @Override
    public void finalizeExport() throws DataExportException {
        try {
            FileOutputStream outputStream = new FileOutputStream(outputFullFilepath);
            workbook.write(outputStream);
        } catch (FileNotFoundException e) {
            throw new DataExportException("file " + outputFullFilepath + " not found", e);
        } catch (IOException e) {
            throw new DataExportException("failed writing data", e);
        }
        finally {
            try {
                workbook.close();
            } catch (IOException e) {
                throw new DataExportException("failed closing file " + outputFullFilepath, e);
            }
        }
    }
}
