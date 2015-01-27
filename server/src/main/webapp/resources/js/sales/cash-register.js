var app = angular.module("KubikCashRegister", []);
var kukikCustomerSearch = new KubikCustomerSearch({
	app : app,
	customerUrl : "customer",
	modal : true,
	$container : $(".customers-modal")
});

app.controller("KubikCashRegisterController", function($scope, $http, $timeout){
	$scope.$on("openCustomerCard", function(event, customer){
		$scope.kubikCustomerCard.openCard(customer);
	})
	
	$scope.$on("saleOver", function(){
		$scope.loadInvoices();
	});
	
	$scope.addOneProduct = function(product){
		// Checks if the product is already found within the invoice.
		var productInInvoice = false;
		for(var detailIndex in $scope.invoice.details){
			var detail = $scope.invoice.details[detailIndex];
			
			if(detail.product.ean13 == product.ean13 && detail.product.supplier.ean13 == product.supplier.ean13){
				detail.quantity += 1;
				productInInvoice = true;
				
				break;
			}
		}
		
		if(!productInInvoice){
			$scope.invoice.details.push({
				invoice : {id : $scope.invoice.id},
				product : {
					id : product.id,
					ean13 : product.ean13,
					supplier : {
						id : product.supplier.id
					}
				},
				quantity : 1
			});	
		}
		
		$scope.saveInvoice();
	};
	
	$scope.cancelInvoice = function(){
		$scope.invoice.status = {type : "CANCELED"};
		
		$scope.saveInvoice();
		
		$(".confirm-cancel").modal("hide");
	};
	
	$scope.checkoutInvoice = function(){
		$scope.$broadcast("checkoutInvoice", $scope.invoice);
	};
	
	$scope.confirmCancelInvoice = function(){
		$(".confirm-cancel").modal({
			backdrop : "static",
			keyboard : "false"
		});
	};
	
	$scope.isSelectedInvoice = function(invoice){
		if($scope.invoice == undefined){
			return false;
		}
		
		return $scope.invoice.id == invoice.id;
	}
	
	$scope.loadInvoices = function(){
		$http.get("cashRegister/session/invoice").success(function(invoices){
			$scope.invoices = invoices;
			
			$scope.searchInProgress = false;
			
			if($scope.invoice == null){
				$scope.showInvoice(invoices[0]);
			}else{
				var invoiceFound = false;
				for(var invoiceIndex in invoices){
					var invoice = invoices[invoiceIndex];
					
					if(invoice.id == $scope.invoice.id){
						$scope.showInvoice(invoice);
						invoiceFound = true;
					}
				}
				
				if(!invoiceFound){
					$scope.showInvoice(invoices[0]);
				}
			}
			
			$timeout(function(){
				if($scope.inputIdToFocus != undefined){
					$("#" + $scope.inputIdToFocus).focus();
				}
			})
		});
	};
	
	$scope.newInvoice = function(){
		$http.get("cashRegister/session/invoice?new").success(function(invoice){
			$scope.invoice = invoice;
			
			$scope.loadInvoices();
		});
	};
	
	$scope.quantityChanged = function($event){
		$scope.inputIdToFocus = $event.target.id;
		if($scope.quantityChangedTimer != undefined) clearTimeout($scope.quantityChangedTimer);
	    
		$scope.quantityChangedTimer = setTimeout($scope.saveInvoice, 1000);
	}
	
	$scope.removeCustomerFromInvoice = function(){
		$scope.invoice.customer = null;
		
		$scope.saveInvoice();
	};
	
	$scope.removeInvoiceDetail = function(invoiceDetail){
		for(var detailIndex in $scope.invoice.details){
			if($scope.invoice.details[detailIndex].id == invoiceDetail.id){
				$scope.invoice.details.splice(detailIndex, 1);
				
				break;
			}
		}
		
		$scope.saveInvoice();
	};
	
	$scope.saveInvoice = function(){
		for(var detailIndex in $scope.invoice.details){
			var detail = $scope.invoice.details[detailIndex];
						
			detail.product = {
				id : detail.product.id,
				ean13 : detail.product.ean13,
				supplier : {id : detail.product.supplier.id}
			};
		}
		
		$http.post("invoice", $scope.invoice).success(function(){
			$scope.loadInvoices();
		});
	};
	
	$scope.searchProduct = function(){
		if($scope.ean13 != "" && !$scope.searchInProgress){
			$scope.searchInProgress = true;
			$http.get("productSearch/" + $scope.ean13 + "?importedInKubik").success(function(productSearch){
				// Check if a product was found
				if(productSearch.products.length == 0){
					// Show a warning explaining that the product does not exists.
				}else if (productSearch.products.length > 1){
					// Show modal to ask product confirmation.
					$scope.productSearch = productSearch;
					
					$("choose-product-modal").modal({
						backdrop : "static",
						keyboard : "false"
					});
				}else{
					$scope.addOneProduct(productSearch.products[0]);
				}
			});

			$scope.ean13 = "";
		}
	}
	
	$scope.showInvoice = function(invoice){
		$timeout(function(){
			$scope.invoice = invoice;
			
			$(".invoice-tabs > li").removeClass("active");
			$("#invoice-tab-" + invoice.id).addClass("active");
			
			if(invoice.details.length == 0){
				$(".checkout").attr("disabled", "disabled");
			}else{
				$(".checkout").removeAttr("disabled");
			}
		});
	}
	
	// Initialise SearchReferenceController
	$scope.kubikReferenceSearch = new KubikReferenceSearch({
		searchUrl : "reference",
		searchResultUrl : "reference",
		$modalContainer : $(".search-reference"),
		importedInKubik : true,
		productUrl : "product",
		productSelected : function(product){
			// Add the product to the basket.
			$scope.addOneProduct(product);
		}
	});
	
	$scope.kubikProductCard = new KubikProductCard({productUrl : "product", productSaved : function(){
		$scope.loadInvoices();
	}});
	
	// this.modal && htmlTemplateUrl != undefined && this.$container != undefined
	$scope.kubikCustomerSearch = kukikCustomerSearch;
	$scope.kubikCustomerSearch.customerSelected = function(customer){
		$scope.invoice.customer = customer;
		
		$scope.saveInvoice();
		
		$scope.kubikCustomerSearch.closeSearchModal();
	};
	
	$scope.kubikCustomerCard = new KubikCustomerCard({
		customerUrl : "customer", 
		customerSaved : function(){
			// Assign current customer to current invoice.
			
		}
	});
	
	$scope.loadInvoices();
});

app.controller("KubikPaymentController", function($scope, $http, $timeout){
	$scope.$on("checkoutInvoice", function(event, invoice){
		invoice.payments = [];
		
		$scope.invoice = invoice;		
		$scope.amountLeft = invoice.totalAmount;
		
		// Open the modal.
		$(".payment-modal").modal({
			backdrop : "static",
			keyboard : "false"
		}).on("shown.bs.modal", function(){
			$(".payment-amount").focus();
		});
	});
	
	$scope.$on("newPaymentMethod", function(event, paymentMethod){
		$scope.paymentMethod = paymentMethod;

		$scope.$broadcast("inactivatePaymentMethodStep");
		$scope.$broadcast("activePaymentAmountStep");
	});
	
	$scope.amountKeyPressed = function(event){
		if((event.keycode >= 48 && event.keycode <= 57) || (event.keycode >= 96 && event.keycode <= 105)){
			if($scope.amount == 0){
				$scope.amount = "";
			}
		}else if(event.keycode == 188 || event.keycode == 190){
			$scope.dotEntered = true;
		}else{
			return false;
		}
	};
	
	$scope.closeSale = function(){
		$(".payment-modal").modal("hide");
		
		$scope.$emit("saleOver");
	}
	
	$scope.loadPaymentMethods = function(){
		// Check the card option by default.
		$http.get("paymentMethod").success(function(paymentMethods){
			$scope.paymentMethods = paymentMethods;
			
			$timeout(function(){
				$("#payment-method-CARD").click();
			}); 
		});
	};
	
	$scope.openReceipt = function(){
		window.open("invoice/" + $scope.invoice.id + "/receipt", "Ticket de caisse", "height=600,width=479");
		
		$scope.closeSale();
	}
	
	$scope.printReceipt = function(){
		$http.post("invoice/" + $scope.invoice.id + "/receipt?print");
		
		$scope.closeSale();
	}
	
	$scope.selectPaymentMethod = function(paymentMethod){
		$scope.paymentMethod = paymentMethod;
	};
	
	$scope.validatePayment = function(){
		var $paymentAmount = $(".payment-amount");
		var newPayment = {
			invoice : {id : $scope.invoice.id},
			amount : parseFloat($paymentAmount.val()),
			paymentMethod : $scope.paymentMethod
		};
		
		$scope.invoice.payments.push(newPayment);
		
		$scope.amountLeft -= newPayment.amount;
		
		if($scope.amountLeft <= 0){
			$scope.invoice.status = {type : "PAID"};
			
			$http.post("invoice", $scope.invoice);
		}
		
		$paymentAmount.val("");
	};
	
	$scope.loadPaymentMethods();
});