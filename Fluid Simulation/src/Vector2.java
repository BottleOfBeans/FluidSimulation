public class Vector2 {
    
    public float x;
    public float y;

    public Vector2(float x,float y){
        this.x = x;
        this.y = y;
    }
    public Vector2(){
        this.x = 0;
        this.y = 0;
    }

    public Vector2 normal(){
        float z = (float) Math.sqrt(this.x * this.x + this.y * this.y);
        return (new Vector2(x/z, y/z));
    }

    public Vector2 add(Vector2 givenVector){
        return (new Vector2(this.x + givenVector.x, this.y + givenVector.y));
    }
    public Vector2 subtract(Vector2 givenVector){
        return (new Vector2(this.x - givenVector.x, this.y - givenVector.y));
    }


    public Vector2 scale(float scale){
        return new Vector2(this.x * scale, this.y * scale);
    }
    public float magnitude(){
            return (float) Math.sqrt(this.x*this.x + this.y*this.y);

    }

}
