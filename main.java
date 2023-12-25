import java.util.Scanner;
import java.util.ArrayList;
import java.util.function.BooleanSupplier;

class Calculator{
    private Stock[] inventory = new Stock[3];
    private ArrayList<Cart> customerCarts = new ArrayList<Cart>();

    private Boolean isCurrentCustomerAMember = false;

    public static void main(String[] args){
        Calculator myself = new Calculator();

        myself.seedData();

        Scanner userInput = new Scanner(System.in);

        Boolean keepTransaction = true;
        while(keepTransaction){
            myself.clearData();
            myself.runTransaction(userInput);
            keepTransaction = myself.askUserForNewTransaction(userInput);
        }

        userInput.close();
    }

    public void runTransaction(Scanner userInput){
        Boolean userInputLoop = true;
        while(userInputLoop){
            userInputLoop = this.askUserForTransaction(userInput);
        }
        
        this.askUserIsAMember(userInput);
        this.calculateCustomerItems();
    }

    public void printInventory(){
        System.out.println("\n*** DAFTAR BARANG ***");
        for(int i = 0; i < this.inventory.length; i++){
            this.printInventoryItem(i + 1, this.inventory[i]);
        }
    }

    public void printInventoryItem(int index, Stock inventory){
        String printTemplate = "%s. %s [%s] <%s>";
        Barang barang = inventory.getBarang();
        System.out.println(
            String.format(
                printTemplate, 
                index, 
                barang.getName(),
                barang.getSku(),
                barang.getPrice()
            )
        );
    }

    public Boolean askUserForTransaction(Scanner userInput){       
        this.printInventory();

        System.out.print("Masukkan index barang : ");

        int indexBarang = userInput.nextInt();

        Stock inventory = this.inventory[indexBarang - 1];
        
        int quantity = this.askUserForTransactionQuantity(userInput, inventory);
        while(quantity < 0){
            System.out.println("STOCK TIDAK MENCUKUPI! (stock sementara : "+ inventory.stock +").");
            quantity = this.askUserForTransactionQuantity(userInput, inventory);
        }
        
        this.addToCart(inventory.getBarang(), quantity);

        return this.askUserForMoreTransaction(userInput);
    }

    public int askUserForTransactionQuantity(Scanner userInput, Stock inventory){
        System.out.print("Masukkan kuantitas dibeli ("+ inventory.getBarang().getName() +" --> stock : "+ inventory.stock +") : ");
        int quantity = userInput.nextInt();
        int totalQuantity = quantity;

        int cartIndex = this.findCartIndexBySku(inventory.getBarang().getSku());
        if( cartIndex >= 0 ){
            totalQuantity += this.customerCarts.get(cartIndex).quantity;
        }

        if( inventory.stock - totalQuantity < 0 ){
            return -1;
        }

        return quantity;
    }

    public Boolean askUserForMoreTransaction(Scanner userInput){
        System.out.print("Apakah ada yang ingin di beli lagi? (y/n): ");
        String userAnswer = userInput.next();
        return userAnswer.charAt(0) == 'y';
    }

    public void askUserIsAMember(Scanner userInput){
        System.out.print("Apakah anda seorang member? (y/n): ");
        String userAnswer = userInput.next();
        this.isCurrentCustomerAMember = userAnswer.charAt(0) == 'y';
    }

    public Boolean askUserForNewTransaction(Scanner userInput){
        System.out.print("Apakah ingin melakukan transaksi baru? (y/n): ");
        String userAnswer = userInput.next();
        return userAnswer.charAt(0) == 'y';
    }

    public void calculateCustomerItems(){
        System.out.print(String.format("\033[2J"));
        System.out.println("\n\n<--- BARANG YANG AKAN DIBELI --->");

        String ITEM_TEMPLATE = "%s. %s (%s) --> %s --> subtotal: %s (satuan: %s)";
        Double grandtotal = Double.valueOf(0);
        for( int i = 0; i < this.customerCarts.size(); i++ ){
            Cart cart = this.customerCarts.get(i);

            Double subtotal = (cart.getBarang().getPrice() * cart.quantity) - (this.getDiscountValue(cart) * cart.quantity);
            grandtotal += subtotal;
            System.out.println(
                String.format(
                    ITEM_TEMPLATE,
                    i + 1,
                    cart.getBarang().getName(),
                    cart.getBarang().getSku(),
                    cart.quantity,
                    subtotal,
                    cart.getBarang().getPrice()
                )
            );

            int barangIndex = this.findBarangIndexBySku(cart.getBarang().getSku());
            if( barangIndex >= 0 ){
                this.inventory[barangIndex].stock -= cart.quantity;
                if(this.inventory[barangIndex].stock < 0) this.inventory[barangIndex].stock = 0;
            }
        }

        System.out.println("GRAND TOTAL --> Rp "+ grandtotal);
    }

    private Double getDiscountValue(Cart cart){
        if( this.isCurrentCustomerAMember && cart.quantity >= 10 ){
            return cart.getBarang().getPrice() * 0.10;
        }

        if( this.isCurrentCustomerAMember && cart.quantity >= 25 ){
            return cart.getBarang().getPrice() * 0.15;
        }

        if( this.isCurrentCustomerAMember && cart.quantity >= 100 ){
            return cart.getBarang().getPrice() * 0.25;
        }

        return Double.valueOf(0);
    }

    public void addToCart(Barang barang, int quantity){
        int cartIndex = this.findCartIndexBySku(barang.getSku());
        if( cartIndex >= 0 ){
            Cart currentCart = this.customerCarts.get(cartIndex);
            currentCart.quantity += quantity;
            this.customerCarts.set(cartIndex, currentCart);
            return;
        }

        this.customerCarts.add(new Cart(barang, quantity));
    }

    public int findBarangIndexBySku(String sku){
        for(int i = 0; i < this.inventory.length; i++){
            if(this.inventory[i].getBarang().getSku() == sku){
                return i;
            }
        }

        return -1;
    }

    public int findCartIndexBySku(String sku){
        for(int i = 0; i < this.customerCarts.size(); i++){
            if(this.customerCarts.get(i).getBarang().getSku() == sku){
                return i;
            }
        }

        return -1;
    }

    public void clearData(){
        this.isCurrentCustomerAMember = false;
        this.customerCarts = new ArrayList<Cart>();
    }

    public void seedData(){
        this.inventory[0] = new Stock(
            new Barang(
                "Pencil B2",
                Double.valueOf(2000),
                "SKU0001"
            ), 
            10
        );

        this.inventory[1] = new Stock(
            new Barang(
                "Pencil B1",
                Double.valueOf(1000),
                "SKU0002"
            ),
            25
        );

        this.inventory[2] = new Stock(
            new Barang(
                "Penghapus",
                Double.valueOf(3000),
                "SKU0003"
            ),
            30
        );
    }
}

class Barang{
    private Double price;
    private String name;
    private String sku; 

    Barang(String name, Double price, String sku){
        this.price = price;
        this.name = name;
        this.sku = sku;
    }

    public Double getPrice(){
        return this.price;
    }

    public String getName(){
        return this.name;
    }

    public String getSku(){
        return this.sku;
    }
}

class Stock{
    private Barang barang;
    public int stock;

    Stock(Barang barang, int stock){
        this.barang = barang;
        this.stock = stock;
    }

    public Barang getBarang(){
        return this.barang;
    }
}

class Cart{
    private Barang barang;
    public int quantity;

    Cart(Barang barang, int quantity){
        this.barang = barang;
        this.quantity = quantity;
    }

    public Barang getBarang(){
        return this.barang;
    }
}