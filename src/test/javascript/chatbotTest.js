//type "mvn jasmine:bdd" and open the 'localhost' link to see this tests

describe("Modal checker", function () {
	
	jasmine.getFixtures().fixturesPath = '/src/templates';
	
	
	it("should call click function", function() {
		
		loadFixtures('admin.html');
		
		
		$('#btnAddThread').click();
		console.log($('#addThreadModal').attr('class'))
		expect($('#addThreadModal').attr('class')).toEqual('Modal Fade Show')

	});
});
