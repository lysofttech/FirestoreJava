
private void FBLoadAll() {
        mdl.Log("All loading...");
        FBData object = new FBData(context, "TableName");
		object.addWhere("Username", Username); // add as many where statments
        object.addOrderBy("Dated", true); // add as many order statments
        object.setFBClassListener(new FBDataListener() {
            @Override
            public void onDataAvailable(Task<QuerySnapshot> task) {
                List<FBTableData> data = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    FBTableData c = document.toObject(FBTableData.class);
                    data.add(c);
                }
                ShowData(data);
            }

            @Override
            public void onNoData(String data) {
                //CreateButtonsNoData();
            }
        });
        object.setLimited(true);
        object.setLimit(50);
        object.FetchData();
}
