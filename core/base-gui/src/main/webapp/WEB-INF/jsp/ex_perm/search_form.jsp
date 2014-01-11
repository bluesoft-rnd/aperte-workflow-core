 <!-- ************************************************* -->
						<div class="form-horizontal">
							<div class="form-group input-group-sm ">
								<label for="exampleInputEmail1" class="col-sm-2 control-label">Kategorie</label>
								<div  class="col-sm-10">
									<button class="btn btn-primary btn-sm" data-toggle="modal" data-target="#categoryModal" id="apw_category_button"><span class="glyphicon glyphicon-list"></span> Wybierz</button>
									 Wybrano kategorii: <span id="category_info">0</span>
								</div>
							</div>

							<div class="form-group input-group-sm ">
								<label for="perm_type" class="col-sm-2 control-label">Typ</label>
								<div  class="col-sm-2">
									<select class="form-control" id="perm_type">
										<option value="">Dowolny</option>
										<option value="">Akceptacja</option>
										<option value="">Weryfikacja</option>
									</select>
								</div>
								<div id="nr-projektu">
									<label for="perm_company" class="col-sm-2 control-label">Spółka</label>
									<div  class="col-sm-6">
										<select class="form-control" id="perm_company">
											<option value="">Dowolna</option>
											<option value="">DPD Polska</option>
											<option value="">ACP</option>
										</select>
									</div>
								</div>
							</div>

							<div class="form-group input-group-sm ">
								<label for="exampleInputEmail1" class="col-sm-2 control-label">Jednostka Organizacyjna</label>
								<div  class="col-sm-10">
									<button class="btn"><span class="glyphicon glyphicon-list"></span> Wybierz</button>
								</div>
							</div>



							<div class="form-group input-group-sm">
								<label for="exampleInputEmail1" class="col-sm-2 control-label">Konto Kosztowe</label>
								<div  class="col-sm-10">
									<select class="form-control" id="konto">
										<option>dowolne</option>
										<option>5000000001 - Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.</option>
										<option>5500000001 - Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.</option>
										<option>5500000005 - Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.</option>
										<option>1000000001 - Konto nieprzypisane do jednostki organizacyjnej osoby składającej zapotrzebowanie ani odpowiedzialnej!!</option>
									</select>
								</div>
							</div>

							<div class="form-group input-group-sm">
								<label for="exampleInputEmail1" class="col-sm-2 control-label">Kwota</label>
								<div  class="col-sm-2">
									<div class="input-group">
									  <span class="input-group-addon">zł</span>
									  <input type="text" class="form-control" placeholder="Wszystkie">
									  <span class="input-group-addon">.00</span>
									</div>
								</div>

							</div>
						</div>
						<button class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> Pokaż</button>
			<!-- ************************************************* -->