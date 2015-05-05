/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package textmining;
import java.util.Comparator;
/**
 *
 * @author Rick
 */
class TfDfIdfCount {
    public String word;
    public int tf_number=0;
    public int df_number=0;
    public double tf_ifd_number=0;
    static int number=0;
    
    public static Comparator<TfDfIdfCount> tdidfcomparator = new Comparator<TfDfIdfCount>(){
        public int compare(TfDfIdfCount t1,TfDfIdfCount t2){
            number++;
            if (t1.tf_ifd_number < t2.tf_ifd_number) return 1;
            if (t1.tf_ifd_number > t2.tf_ifd_number) return -1;
            return 0;
        }
    };
    
}
